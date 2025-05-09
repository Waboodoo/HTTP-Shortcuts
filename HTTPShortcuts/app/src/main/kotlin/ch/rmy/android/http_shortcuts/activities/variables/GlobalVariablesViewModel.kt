package ch.rmy.android.http_shortcuts.activities.variables

import android.app.Application
import androidx.lifecycle.viewModelScope
import ch.rmy.android.framework.extensions.swapped
import ch.rmy.android.framework.extensions.tryOrLog
import ch.rmy.android.framework.utils.localization.StringResLocalizable
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.variables.GlobalVariablesViewModel.InitData
import ch.rmy.android.http_shortcuts.activities.variables.VariableTypeMappings.getTypeName
import ch.rmy.android.http_shortcuts.activities.variables.models.GlobalVariableListItem
import ch.rmy.android.http_shortcuts.activities.variables.usecases.GenerateVariableKeyUseCase
import ch.rmy.android.http_shortcuts.activities.variables.usecases.GetUsedGlobalVariableIdsUseCase
import ch.rmy.android.http_shortcuts.data.domains.request_headers.RequestHeaderRepository
import ch.rmy.android.http_shortcuts.data.domains.request_parameters.RequestParameterRepository
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.domains.variables.GlobalVariableId
import ch.rmy.android.http_shortcuts.data.domains.variables.GlobalVariableRepository
import ch.rmy.android.http_shortcuts.data.enums.VariableType
import ch.rmy.android.http_shortcuts.data.models.GlobalVariable
import ch.rmy.android.http_shortcuts.extensions.getRequestParametersForShortcuts
import ch.rmy.android.http_shortcuts.extensions.ids
import ch.rmy.android.http_shortcuts.navigation.NavigationDestination
import ch.rmy.android.http_shortcuts.utils.ExternalURLs
import ch.rmy.android.http_shortcuts.variables.VariableManager
import ch.rmy.android.http_shortcuts.variables.VariableResolver
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class GlobalVariablesViewModel
@Inject
constructor(
    application: Application,
    private val globalVariableRepository: GlobalVariableRepository,
    private val shortcutRepository: ShortcutRepository,
    private val requestHeaderRepository: RequestHeaderRepository,
    private val requestParameterRepository: RequestParameterRepository,
    private val getUsedGlobalVariableIdsUseCase: GetUsedGlobalVariableIdsUseCase,
    private val generateVariableKey: GenerateVariableKeyUseCase,
) : BaseViewModel<InitData, VariablesViewState>(application) {

    private var activeGlobalVariableId: GlobalVariableId? = null
    private var globalVariablesInitialized = false
    private var globalVariables: List<GlobalVariable> = emptyList()
        set(value) {
            field = value
            globalVariablesInitialized = true
        }
    private var usedGlobalVariableIds: Set<GlobalVariableId>? = null
        set(value) {
            if (field != value) {
                field = value
                if (globalVariablesInitialized) {
                    viewModelScope.launch {
                        recomputeGlobalVariablesInViewState()
                    }
                }
            }
        }

    override suspend fun initialize(data: InitData): VariablesViewState {
        val variablesFlow = globalVariableRepository.observeVariables()
        globalVariables = variablesFlow.first()

        viewModelScope.launch {
            variablesFlow
                .collect { variables ->
                    this@GlobalVariablesViewModel.globalVariables = variables
                    recomputeGlobalVariablesInViewState()
                    recomputeUsedVariableIds()
                }
        }
        return VariablesViewState(
            variables = mapVariables(globalVariables),
        )
    }

    private suspend fun recomputeGlobalVariablesInViewState() {
        updateViewState {
            copy(variables = mapVariables(this@GlobalVariablesViewModel.globalVariables))
        }
    }

    private fun mapVariables(variables: List<GlobalVariable>): List<GlobalVariableListItem> =
        variables.map { variable ->
            GlobalVariableListItem(
                id = variable.id,
                key = variable.key,
                type = StringResLocalizable(variable.type.getTypeName()),
                isUnused = usedGlobalVariableIds?.contains(variable.id) == false,
            )
        }

    fun onVariableMoved(globalVariableId1: GlobalVariableId, globalVariableId2: GlobalVariableId) = runAction {
        updateViewState {
            copy(variables = variables.swapped(globalVariableId1, globalVariableId2) { id })
        }
        withProgressTracking {
            globalVariableRepository.moveVariable(globalVariableId1, globalVariableId2)
        }
    }

    fun onCreateButtonClicked() = runAction {
        updateDialogState(GlobalVariablesDialogState.Creation)
    }

    fun onHelpButtonClicked() = runAction {
        openURL(ExternalURLs.VARIABLES_DOCUMENTATION)
    }

    fun onVariableClicked(globalVariableId: GlobalVariableId) = runAction {
        val variable = getVariable(globalVariableId) ?: skipAction()
        activeGlobalVariableId = globalVariableId
        updateDialogState(
            GlobalVariablesDialogState.ContextMenu(
                showUse = initData.asPicker,
                variableKey = variable.key,
            ),
        )
    }

    private fun getVariable(globalVariableId: GlobalVariableId) =
        globalVariables.firstOrNull { it.id == globalVariableId }

    fun onCreationDialogVariableTypeSelected(variableType: VariableType) = runAction {
        updateDialogState(null)
        navigate(NavigationDestination.GlobalVariableEditor.buildRequest(variableType))
    }

    fun onUseSelected() = runAction {
        updateDialogState(null)
        val variableId = activeGlobalVariableId ?: skipAction()
        closeScreen(result = NavigationDestination.GlobalVariables.VariableSelectedResult(variableId))
    }

    fun onEditOptionSelected() = runAction {
        updateDialogState(null)
        val variableId = activeGlobalVariableId ?: skipAction()
        val variable = getVariable(variableId) ?: skipAction()
        navigate(NavigationDestination.GlobalVariableEditor.buildRequest(variable.type, variableId))
    }

    fun onDuplicateOptionSelected() = runAction {
        updateDialogState(null)
        val variableId = activeGlobalVariableId ?: skipAction()
        val variable = getVariable(variableId) ?: skipAction()
        withProgressTracking {
            val newKey = generateVariableKey(variable.key, globalVariables.map { it.key })
            globalVariableRepository.duplicateVariable(variableId, newKey)
            showSnackbar(StringResLocalizable(R.string.message_variable_duplicated, variable.key))
        }
    }

    fun onDeletionOptionSelected() = runAction {
        updateDialogState(null)
        val variableId = activeGlobalVariableId ?: skipAction()
        val variable = getVariable(variableId) ?: skipAction()
        val shortcutNames = withContext(Dispatchers.Default) {
            getShortcutNamesWhereVariableIsInUse(variableId)
        }
        updateDialogState(
            GlobalVariablesDialogState.Delete(
                variableKey = variable.key,
                shortcutNames = shortcutNames,
            ),
        )
    }

    private suspend fun getShortcutNamesWhereVariableIsInUse(globalVariableId: GlobalVariableId): List<String> {
        val variableManager = VariableManager(globalVariables)
        // TODO(???): Also check if the variable is used inside another variable

        val shortcuts = shortcutRepository.getShortcuts()
        val headersByShortcutId = requestHeaderRepository.getRequestHeadersByShortcutIds(shortcuts.ids())
        val parametersByShortcutId = requestParameterRepository.getRequestParametersForShortcuts(shortcuts)

        return shortcuts
            .filter { shortcut ->
                VariableResolver.extractGlobalVariableIdsIncludingScripting(
                    shortcut = shortcut,
                    headers = headersByShortcutId[shortcut.id] ?: emptyList(),
                    parameters = parametersByShortcutId[shortcut.id] ?: emptyList(),
                    variableManager = variableManager,
                )
                    .contains(globalVariableId)
            }
            .map { shortcut ->
                shortcut.name
            }
            .distinct()
    }

    fun onDeletionConfirmed() = runAction {
        updateDialogState(null)
        val variableId = activeGlobalVariableId ?: skipAction()
        val variable = getVariable(variableId) ?: skipAction()
        withProgressTracking {
            globalVariableRepository.deleteVariable(variableId)
            showSnackbar(StringResLocalizable(R.string.variable_deleted, variable.key))
            recomputeUsedVariableIds()
        }
    }

    fun onBackPressed() = runAction {
        waitForOperationsToFinish()
        closeScreen()
    }

    private suspend fun recomputeUsedVariableIds() {
        tryOrLog {
            usedGlobalVariableIds = withContext(Dispatchers.Default) {
                getUsedGlobalVariableIdsUseCase()
            }
        }
    }

    fun onSortButtonClicked() = runAction {
        withProgressTracking {
            globalVariableRepository.sortVariablesAlphabetically()
            showSnackbar(R.string.message_variables_sorted)
        }
    }

    fun onDialogDismissed() = runAction {
        updateDialogState(null)
    }

    private suspend fun updateDialogState(dialogState: GlobalVariablesDialogState?) {
        updateViewState {
            copy(dialogState = dialogState)
        }
    }

    fun onChangesDiscarded() = runAction {
        showSnackbar(R.string.message_changes_discarded)
    }

    fun onVariableCreated(globalVariableId: GlobalVariableId) = runAction {
        if (initData.asPicker) {
            closeScreen(result = NavigationDestination.GlobalVariables.VariableSelectedResult(globalVariableId))
        }
    }

    data class InitData(
        val asPicker: Boolean,
    )
}

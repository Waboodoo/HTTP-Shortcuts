package ch.rmy.android.http_shortcuts.activities.variables

import android.app.Application
import androidx.lifecycle.viewModelScope
import ch.rmy.android.framework.extensions.swapped
import ch.rmy.android.framework.extensions.tryOrLog
import ch.rmy.android.framework.utils.localization.StringResLocalizable
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.variables.VariableTypeMappings.getTypeName
import ch.rmy.android.http_shortcuts.activities.variables.VariablesViewModel.InitData
import ch.rmy.android.http_shortcuts.activities.variables.models.VariableListItem
import ch.rmy.android.http_shortcuts.activities.variables.usecases.GenerateVariableKeyUseCase
import ch.rmy.android.http_shortcuts.activities.variables.usecases.GetUsedVariableIdsUseCase
import ch.rmy.android.http_shortcuts.data.domains.request_headers.RequestHeaderRepository
import ch.rmy.android.http_shortcuts.data.domains.request_parameters.RequestParameterRepository
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableId
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableRepository
import ch.rmy.android.http_shortcuts.data.enums.VariableType
import ch.rmy.android.http_shortcuts.data.models.Variable
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
class VariablesViewModel
@Inject
constructor(
    application: Application,
    private val variableRepository: VariableRepository,
    private val shortcutRepository: ShortcutRepository,
    private val requestHeaderRepository: RequestHeaderRepository,
    private val requestParameterRepository: RequestParameterRepository,
    private val getUsedVariableIdsUseCase: GetUsedVariableIdsUseCase,
    private val generateVariableKey: GenerateVariableKeyUseCase,
) : BaseViewModel<InitData, VariablesViewState>(application) {

    private var activeVariableId: VariableId? = null
    private var variablesInitialized = false
    private var variables: List<Variable> = emptyList()
        set(value) {
            field = value
            variablesInitialized = true
        }
    private var usedVariableIds: Set<VariableId>? = null
        set(value) {
            if (field != value) {
                field = value
                if (variablesInitialized) {
                    viewModelScope.launch {
                        recomputeVariablesInViewState()
                    }
                }
            }
        }

    override suspend fun initialize(data: InitData): VariablesViewState {
        val variablesFlow = variableRepository.observeVariables()
        variables = variablesFlow.first()

        viewModelScope.launch {
            variablesFlow
                .collect { variables ->
                    this@VariablesViewModel.variables = variables
                    recomputeVariablesInViewState()
                    recomputeUsedVariableIds()
                }
        }
        return VariablesViewState(
            variables = mapVariables(variables),
        )
    }

    private suspend fun recomputeVariablesInViewState() {
        updateViewState {
            copy(variables = mapVariables(this@VariablesViewModel.variables))
        }
    }

    private fun mapVariables(variables: List<Variable>): List<VariableListItem> =
        variables.map { variable ->
            VariableListItem(
                id = variable.id,
                key = variable.key,
                type = StringResLocalizable(variable.type.getTypeName()),
                isUnused = usedVariableIds?.contains(variable.id) == false,
            )
        }

    fun onVariableMoved(variableId1: VariableId, variableId2: VariableId) = runAction {
        updateViewState {
            copy(variables = variables.swapped(variableId1, variableId2) { id })
        }
        withProgressTracking {
            variableRepository.moveVariable(variableId1, variableId2)
        }
    }

    fun onCreateButtonClicked() = runAction {
        updateDialogState(VariablesDialogState.Creation)
    }

    fun onHelpButtonClicked() = runAction {
        openURL(ExternalURLs.VARIABLES_DOCUMENTATION)
    }

    fun onVariableClicked(variableId: VariableId) = runAction {
        val variable = getVariable(variableId) ?: skipAction()
        activeVariableId = variableId
        updateDialogState(
            VariablesDialogState.ContextMenu(
                showUse = initData.asPicker,
                variableKey = variable.key,
            ),
        )
    }

    private fun getVariable(variableId: VariableId) =
        variables.firstOrNull { it.id == variableId }

    fun onCreationDialogVariableTypeSelected(variableType: VariableType) = runAction {
        updateDialogState(null)
        navigate(NavigationDestination.VariableEditor.buildRequest(variableType))
    }

    fun onUseSelected() = runAction {
        updateDialogState(null)
        val variableId = activeVariableId ?: skipAction()
        closeScreen(result = NavigationDestination.Variables.VariableSelectedResult(variableId))
    }

    fun onEditOptionSelected() = runAction {
        updateDialogState(null)
        val variableId = activeVariableId ?: skipAction()
        val variable = getVariable(variableId) ?: skipAction()
        navigate(NavigationDestination.VariableEditor.buildRequest(variable.type, variableId))
    }

    fun onDuplicateOptionSelected() = runAction {
        updateDialogState(null)
        val variableId = activeVariableId ?: skipAction()
        val variable = getVariable(variableId) ?: skipAction()
        withProgressTracking {
            val newKey = generateVariableKey(variable.key, variables.map { it.key })
            variableRepository.duplicateVariable(variableId, newKey)
            showSnackbar(StringResLocalizable(R.string.message_variable_duplicated, variable.key))
        }
    }

    fun onDeletionOptionSelected() = runAction {
        updateDialogState(null)
        val variableId = activeVariableId ?: skipAction()
        val variable = getVariable(variableId) ?: skipAction()
        val shortcutNames = withContext(Dispatchers.Default) {
            getShortcutNamesWhereVariableIsInUse(variableId)
        }
        updateDialogState(
            VariablesDialogState.Delete(
                variableKey = variable.key,
                shortcutNames = shortcutNames,
            ),
        )
    }

    private suspend fun getShortcutNamesWhereVariableIsInUse(variableId: VariableId): List<String> {
        val variableLookup = VariableManager(variables)
        // TODO: Also check if the variable is used inside another variable

        val shortcuts = shortcutRepository.getShortcuts()
        val headersByShortcutId = requestHeaderRepository.getRequestHeadersByShortcutIds(shortcuts.ids())
        val parametersByShortcutId = requestParameterRepository.getRequestParametersForShortcuts(shortcuts)

        return shortcuts
            .filter { shortcut ->
                VariableResolver.extractVariableIdsIncludingScripting(
                    shortcut = shortcut,
                    headers = headersByShortcutId[shortcut.id] ?: emptyList(),
                    parameters = parametersByShortcutId[shortcut.id] ?: emptyList(),
                    variableLookup = variableLookup,
                )
                    .contains(variableId)
            }
            .map { shortcut ->
                shortcut.name
            }
            .distinct()
    }

    fun onDeletionConfirmed() = runAction {
        updateDialogState(null)
        val variableId = activeVariableId ?: skipAction()
        val variable = getVariable(variableId) ?: skipAction()
        withProgressTracking {
            variableRepository.deleteVariable(variableId)
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
            usedVariableIds = withContext(Dispatchers.Default) {
                getUsedVariableIdsUseCase()
            }
        }
    }

    fun onSortButtonClicked() = runAction {
        withProgressTracking {
            variableRepository.sortVariablesAlphabetically()
            showSnackbar(R.string.message_variables_sorted)
        }
    }

    fun onDialogDismissed() = runAction {
        updateDialogState(null)
    }

    private suspend fun updateDialogState(dialogState: VariablesDialogState?) {
        updateViewState {
            copy(dialogState = dialogState)
        }
    }

    fun onChangesDiscarded() = runAction {
        showSnackbar(R.string.message_changes_discarded)
    }

    fun onVariableCreated(variableId: VariableId) = runAction {
        if (initData.asPicker) {
            closeScreen(result = NavigationDestination.Variables.VariableSelectedResult(variableId))
        }
    }

    data class InitData(
        val asPicker: Boolean,
    )
}

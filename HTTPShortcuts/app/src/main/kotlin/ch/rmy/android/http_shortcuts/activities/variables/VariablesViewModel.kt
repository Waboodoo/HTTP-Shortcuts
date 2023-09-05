package ch.rmy.android.http_shortcuts.activities.variables

import android.app.Application
import androidx.lifecycle.viewModelScope
import ch.rmy.android.framework.extensions.swapped
import ch.rmy.android.framework.extensions.tryOrLog
import ch.rmy.android.framework.utils.localization.StringResLocalizable
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.variables.VariableTypeMappings.getTypeName
import ch.rmy.android.http_shortcuts.activities.variables.models.VariableListItem
import ch.rmy.android.http_shortcuts.activities.variables.usecases.GenerateVariableKeyUseCase
import ch.rmy.android.http_shortcuts.activities.variables.usecases.GetUsedVariableIdsUseCase
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableId
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableRepository
import ch.rmy.android.http_shortcuts.data.enums.VariableType
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.navigation.NavigationDestination
import ch.rmy.android.http_shortcuts.utils.ExternalURLs
import ch.rmy.android.http_shortcuts.variables.VariableManager
import ch.rmy.android.http_shortcuts.variables.VariableResolver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class VariablesViewModel
@Inject
constructor(
    application: Application,
    private val variableRepository: VariableRepository,
    private val shortcutRepository: ShortcutRepository,
    private val getUsedVariableIdsUseCase: GetUsedVariableIdsUseCase,
    private val generateVariableKey: GenerateVariableKeyUseCase,
) : BaseViewModel<Unit, VariablesViewState>(application) {

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

    override suspend fun initialize(data: Unit): VariablesViewState {
        val variablesFlow = variableRepository.getObservableVariables()
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
            variables = mapVariables(variables)
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
                type = StringResLocalizable(variable.variableType.getTypeName()),
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
                variableKey = variable.key,
            )
        )
    }

    private fun getVariable(variableId: VariableId) =
        variables.firstOrNull { it.id == variableId }

    fun onCreationDialogVariableTypeSelected(variableType: VariableType) = runAction {
        updateDialogState(null)
        navigate(NavigationDestination.VariableEditor.buildRequest(variableType))
    }

    fun onEditOptionSelected() = runAction {
        updateDialogState(null)
        val variableId = activeVariableId ?: skipAction()
        val variable = getVariable(variableId) ?: skipAction()
        navigate(NavigationDestination.VariableEditor.buildRequest(variable.variableType, variableId))
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
            )
        )
    }

    private suspend fun getShortcutNamesWhereVariableIsInUse(variableId: VariableId): List<String> {
        val variableLookup = VariableManager(variables)
        // TODO: Also check if the variable is used inside another variable
        return shortcutRepository.getShortcuts()
            .filter { shortcut ->
                VariableResolver.extractVariableIdsIncludingScripting(shortcut, variableLookup)
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
}

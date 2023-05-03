package ch.rmy.android.http_shortcuts.activities.variables

import android.app.Application
import androidx.lifecycle.viewModelScope
import ch.rmy.android.framework.extensions.swapped
import ch.rmy.android.framework.extensions.tryOrLog
import ch.rmy.android.framework.utils.localization.StringResLocalizable
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.variables.VariableTypeMappings.getTypeName
import ch.rmy.android.http_shortcuts.activities.variables.editor.VariableEditorActivity
import ch.rmy.android.http_shortcuts.activities.variables.models.VariableListItem
import ch.rmy.android.http_shortcuts.activities.variables.usecases.GenerateVariableKeyUseCase
import ch.rmy.android.http_shortcuts.activities.variables.usecases.GetUsedVariableIdsUseCase
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableId
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableRepository
import ch.rmy.android.http_shortcuts.data.enums.VariableType
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.usecases.KeepVariablePlaceholderProviderUpdatedUseCase
import ch.rmy.android.http_shortcuts.utils.ExternalURLs
import ch.rmy.android.http_shortcuts.variables.VariableManager
import ch.rmy.android.http_shortcuts.variables.VariableResolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class VariablesViewModel(application: Application) : BaseViewModel<Unit, VariablesViewState>(application) {

    @Inject
    lateinit var variableRepository: VariableRepository

    @Inject
    lateinit var shortcutRepository: ShortcutRepository

    @Inject
    lateinit var getUsedVariableIdsUseCase: GetUsedVariableIdsUseCase

    @Inject
    lateinit var keepVariablePlaceholderProviderUpdated: KeepVariablePlaceholderProviderUpdatedUseCase

    @Inject
    lateinit var generateVariableKey: GenerateVariableKeyUseCase

    init {
        getApplicationComponent().inject(this)
    }

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
                    recomputeVariablesInViewState()
                }
            }
        }

    override fun onInitializationStarted(data: Unit) {
        finalizeInitialization(silent = true)
    }

    override fun initViewState() = VariablesViewState()

    override fun onInitialized() {
        viewModelScope.launch {
            variableRepository.getObservableVariables()
                .collect { variables ->
                    this@VariablesViewModel.variables = variables
                    recomputeVariablesInViewState()
                    recomputeUsedVariableIds()
                }
        }

        viewModelScope.launch {
            keepVariablePlaceholderProviderUpdated()
        }
    }

    private fun recomputeVariablesInViewState() {
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

    fun onVariableMoved(variableId1: VariableId, variableId2: VariableId) {
        updateViewState {
            copy(variables = variables.swapped(variableId1, variableId2) { id })
        }
        launchWithProgressTracking {
            variableRepository.moveVariable(variableId1, variableId2)
        }
    }

    fun onCreateButtonClicked() {
        updateDialogState(VariablesDialogState.Creation)
    }

    fun onHelpButtonClicked() {
        openURL(ExternalURLs.VARIABLES_DOCUMENTATION)
    }

    fun onVariableClicked(variableId: VariableId) {
        val variable = getVariable(variableId) ?: return
        activeVariableId = variableId
        updateDialogState(
            VariablesDialogState.ContextMenu(
                variableKey = variable.key,
            )
        )
    }

    private fun getVariable(variableId: VariableId) =
        variables.firstOrNull { it.id == variableId }

    fun onCreationDialogVariableTypeSelected(variableType: VariableType) {
        updateDialogState(null)
        openActivity(
            VariableEditorActivity.IntentBuilder(variableType)
        )
    }

    fun onEditOptionSelected() {
        updateDialogState(null)
        val variableId = activeVariableId ?: return
        val variable = getVariable(variableId) ?: return
        openActivity(
            VariableEditorActivity.IntentBuilder(variable.variableType)
                .variableId(variableId)
        )
    }

    fun onDuplicateOptionSelected() {
        updateDialogState(null)
        val variableId = activeVariableId ?: return
        val variable = getVariable(variableId) ?: return
        launchWithProgressTracking {
            val newKey = generateVariableKey(variable.key, variables.map { it.key })
            variableRepository.duplicateVariable(variableId, newKey)
            showSnackbar(StringResLocalizable(R.string.message_variable_duplicated, variable.key))
        }
    }

    fun onDeletionOptionSelected() {
        updateDialogState(null)
        val variableId = activeVariableId ?: return
        val variable = getVariable(variableId) ?: return
        viewModelScope.launch {
            val shortcutNames = getShortcutNamesWhereVariableIsInUse(variableId)
            updateDialogState(
                VariablesDialogState.Delete(
                    variableKey = variable.key,
                    shortcutNames = shortcutNames,
                )
            )
        }
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

    fun onDeletionConfirmed() {
        updateDialogState(null)
        val variableId = activeVariableId ?: return
        val variable = getVariable(variableId) ?: return
        launchWithProgressTracking {
            variableRepository.deleteVariable(variableId)
            showSnackbar(StringResLocalizable(R.string.variable_deleted, variable.key))
            recomputeUsedVariableIds()
        }
    }

    fun onBackPressed() {
        viewModelScope.launch {
            waitForOperationsToFinish()
            finish()
        }
    }

    private suspend fun recomputeUsedVariableIds() {
        tryOrLog {
            usedVariableIds = withContext(Dispatchers.Default) {
                getUsedVariableIdsUseCase()
            }
        }
    }

    fun onSortButtonClicked() {
        launchWithProgressTracking {
            variableRepository.sortVariablesAlphabetically()
            showSnackbar(R.string.message_variables_sorted)
        }
    }

    fun onDialogDismissed() {
        updateDialogState(null)
    }

    private fun updateDialogState(dialogState: VariablesDialogState?) {
        updateViewState {
            copy(dialogState = dialogState)
        }
    }
}

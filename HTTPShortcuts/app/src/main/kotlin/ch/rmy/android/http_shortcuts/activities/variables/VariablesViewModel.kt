package ch.rmy.android.http_shortcuts.activities.variables

import android.app.Application
import androidx.lifecycle.viewModelScope
import ch.rmy.android.framework.extensions.tryOrLog
import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.framework.utils.localization.StringResLocalizable
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.framework.viewmodel.WithDialog
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.variables.editor.VariableEditorActivity
import ch.rmy.android.http_shortcuts.activities.variables.editor.usecases.GetContextMenuDialogUseCase
import ch.rmy.android.http_shortcuts.activities.variables.editor.usecases.GetDeletionDialogUseCase
import ch.rmy.android.http_shortcuts.activities.variables.usecases.GetCreationDialogUseCase
import ch.rmy.android.http_shortcuts.activities.variables.usecases.GetUsedVariableIdsUseCase
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableId
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableKey
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableRepository
import ch.rmy.android.http_shortcuts.data.enums.VariableType
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.usecases.KeepVariablePlaceholderProviderUpdatedUseCase
import ch.rmy.android.http_shortcuts.utils.ExternalURLs
import ch.rmy.android.http_shortcuts.variables.VariableManager
import ch.rmy.android.http_shortcuts.variables.VariableResolver
import ch.rmy.android.http_shortcuts.variables.Variables.KEY_MAX_LENGTH
import kotlinx.coroutines.launch
import javax.inject.Inject

class VariablesViewModel(application: Application) : BaseViewModel<Unit, VariablesViewState>(application), WithDialog {

    @Inject
    lateinit var variableRepository: VariableRepository

    @Inject
    lateinit var shortcutRepository: ShortcutRepository

    @Inject
    lateinit var getDeletionDialog: GetDeletionDialogUseCase

    @Inject
    lateinit var getContextMenuDialog: GetContextMenuDialogUseCase

    @Inject
    lateinit var getCreationDialog: GetCreationDialogUseCase

    @Inject
    lateinit var getUsedVariableIdsUseCase: GetUsedVariableIdsUseCase

    @Inject
    lateinit var keepVariablePlaceholderProviderUpdated: KeepVariablePlaceholderProviderUpdatedUseCase

    init {
        getApplicationComponent().inject(this)
    }

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

    override var dialogState: DialogState?
        get() = currentViewState?.dialogState
        set(value) {
            updateViewState {
                copy(dialogState = value)
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
            VariableListItem.Variable(
                id = variable.id,
                key = variable.key,
                type = StringResLocalizable(VariableTypeMappings.getTypeName(variable.variableType)),
                isUnused = usedVariableIds?.contains(variable.id) == false,
            )
        }
            .ifEmpty {
                listOf(VariableListItem.EmptyState)
            }

    fun onVariableMoved(variableId1: VariableId, variableId2: VariableId) {
        launchWithProgressTracking {
            variableRepository.moveVariable(variableId1, variableId2)
        }
    }

    fun onCreateButtonClicked() {
        dialogState = getCreationDialog(this)
    }

    fun onHelpButtonClicked() {
        openURL(ExternalURLs.VARIABLES_DOCUMENTATION)
    }

    fun onVariableClicked(variableId: VariableId) {
        val variable = getVariable(variableId) ?: return
        dialogState = getContextMenuDialog(variableId, StringResLocalizable(VariableTypeMappings.getTypeName(variable.variableType)), this)
    }

    private fun getVariable(variableId: VariableId) =
        variables.firstOrNull { it.id == variableId }

    fun onCreationDialogVariableTypeSelected(variableType: VariableType) {
        openActivity(
            VariableEditorActivity.IntentBuilder(variableType)
        )
    }

    fun onEditOptionSelected(variableId: VariableId) {
        val variable = getVariable(variableId) ?: return
        openActivity(
            VariableEditorActivity.IntentBuilder(variable.variableType)
                .variableId(variableId)
        )
    }

    fun onDuplicateOptionSelected(variableId: VariableId) {
        val variable = getVariable(variableId) ?: return
        launchWithProgressTracking {
            val newKey = generateNewKey(variable.key)
            variableRepository.duplicateVariable(variableId, newKey)
            showSnackbar(StringResLocalizable(R.string.message_variable_duplicated, variable.key))
        }
    }

    private fun generateNewKey(oldKey: String): VariableKey {
        val base = oldKey.take(KEY_MAX_LENGTH - 2)
        for (i in 2..99) {
            val newKey = "$base$i"
            if (!isVariableKeyInUse(newKey)) {
                return newKey
            }
        }
        return List(KEY_MAX_LENGTH - 2) {
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".random()
        }.joinToString(separator = "")
    }

    private fun isVariableKeyInUse(key: String): Boolean =
        variables.any { it.key == key }

    fun onDeletionOptionSelected(variableId: VariableId) {
        val variable = getVariable(variableId) ?: return
        viewModelScope.launch {
            val shortcutNames = getShortcutNamesWhereVariableIsInUse(variableId)
            dialogState = getDeletionDialog(
                variableId = variableId,
                title = variable.key,
                message = getDeletionMessage(shortcutNames),
                viewModel = this@VariablesViewModel,
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

    private fun getDeletionMessage(shortcutNames: List<String>): Localizable =
        if (shortcutNames.isEmpty()) {
            StringResLocalizable(R.string.confirm_delete_variable_message)
        } else {
            Localizable.create { context ->
                context.getString(R.string.confirm_delete_variable_message)
                    .plus("\n\n")
                    .plus(
                        context.resources.getQuantityString(
                            R.plurals.warning_variable_still_in_use_in_shortcuts,
                            shortcutNames.size,
                            shortcutNames.joinToString(),
                            shortcutNames.size,
                        )
                    )
            }
        }

    fun onDeletionConfirmed(variableId: VariableId) {
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

    fun onStart() {
        viewModelScope.launch {
            recomputeUsedVariableIds()
        }
    }

    private suspend fun recomputeUsedVariableIds() {
        tryOrLog {
            usedVariableIds = getUsedVariableIdsUseCase()
        }
    }

    fun onSortButtonClicked() {
        launchWithProgressTracking {
            variableRepository.sortVariablesAlphabetically()
            showSnackbar(R.string.message_variables_sorted)
        }
    }
}

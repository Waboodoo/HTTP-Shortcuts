package ch.rmy.android.http_shortcuts.activities.variables.editor

import android.app.Application
import ch.rmy.android.framework.extensions.attachTo
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.utils.UUIDUtils.newUUID
import ch.rmy.android.framework.utils.localization.StringResLocalizable
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.framework.viewmodel.EventBridge
import ch.rmy.android.framework.viewmodel.ViewModelEvent
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.variables.VariableTypeMappings
import ch.rmy.android.http_shortcuts.data.domains.variables.TemporaryVariableRepository
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableRepository
import ch.rmy.android.http_shortcuts.data.enums.VariableType
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.dialogs.DialogBuilder
import ch.rmy.android.http_shortcuts.variables.Variables

class VariableEditorViewModel(application: Application) : BaseViewModel<VariableEditorViewModel.InitData, VariableEditorViewState>(application) {

    private val variableRepository = VariableRepository()
    private val temporaryVariableRepository = TemporaryVariableRepository()

    private val outgoingEventBridge = EventBridge(VariableEditorToVariableTypeEvent::class.java)
    private val incomingEventBridge = EventBridge(VariableTypeToVariableEditorEvent::class.java)

    private val variableId: String?
        get() = initData.variableId
    private val variableType: VariableType
        get() = initData.variableType

    private var oldVariable: Variable? = null
    private lateinit var variable: Variable
    private lateinit var variableKeysInUse: List<String>

    private var isSaving = false

    private var variableKeyInputErrorRes: Int? = null
        set(value) {
            if (field != value) {
                field = value
                updateViewState {
                    copy(variableKeyInputError = value?.let { StringResLocalizable(it) })
                }
                if (value != null) {
                    emitEvent(VariableEditorEvent.FocusVariableKeyInput)
                }
            }
        }

    override fun onInitializationStarted(data: InitData) {
        if (data.variableId != null) {
            variableRepository
                .createTemporaryVariableFromVariable(data.variableId)
        } else {
            temporaryVariableRepository.createNewTemporaryVariable(variableType)
        }
            .subscribe(
                ::onTemporaryVariableCreated,
                ::handleInitializationError,
            )
            .attachTo(destroyer)

        variableRepository.getObservableVariables()
            .subscribe { variables ->
                variableKeysInUse = variables
                    .filter { variable ->
                        variable.id != variableId
                    }
                    .map { variable ->
                        variable.key
                    }
            }
            .attachTo(destroyer)

        incomingEventBridge.events
            .subscribe { event ->
                when (event) {
                    is VariableTypeToVariableEditorEvent.Validated -> onValidated(event.valid)
                }
            }
            .attachTo(destroyer)
    }

    private fun onTemporaryVariableCreated() {
        temporaryVariableRepository.getObservableTemporaryVariable()
            .subscribe { variable ->
                this.variable = variable
                if (oldVariable == null) {
                    oldVariable = variable
                    finalizeInitialization(silent = true)
                }
                updateViewState {
                    copy(
                        variableKey = variable.key,
                        variableTitle = variable.title,
                        urlEncodeChecked = variable.urlEncode,
                        jsonEncodeChecked = variable.jsonEncode,
                        allowShareChecked = variable.isShareText,
                    )
                }
            }
            .attachTo(destroyer)
    }

    private fun handleInitializationError(error: Throwable) {
        handleUnexpectedError(error)
        finish()
    }

    override fun initViewState() = VariableEditorViewState(
        title = StringResLocalizable(if (variableId == null) R.string.create_variable else R.string.edit_variable),
        subtitle = StringResLocalizable(VariableTypeMappings.getTypeName(variableType)),
        titleInputVisible = variableType.hasDialogTitle,
    )

    fun onSaveButtonClicked() {
        trySave()
    }

    private fun trySave() {
        if (isSaving) {
            return
        }
        isSaving = true
        waitForOperationsToFinish {
            if (validate()) {
                outgoingEventBridge.submit(VariableEditorToVariableTypeEvent.Validate)
            } else {
                isSaving = false
            }
        }
    }

    private fun onValidated(valid: Boolean) {
        if (valid) {
            save()
        } else {
            isSaving = false
        }
    }

    private fun save() {
        variableRepository.copyTemporaryVariableToVariable(variableId ?: newUUID())
            .subscribe(
                {
                    finish()
                },
                { error ->
                    isSaving = false
                    showSnackbar(R.string.error_generic)
                    logException(error)
                },
            )
            .attachTo(destroyer)
    }

    private fun validate(): Boolean {
        if (variable.key.isEmpty()) {
            variableKeyInputErrorRes = R.string.validation_key_non_empty
            return false
        }
        if (!Variables.isValidVariableKey(variable.key)) {
            variableKeyInputErrorRes = R.string.warning_invalid_variable_key
            return false
        }
        if (variableKeysInUse.contains(variable.key)) {
            variableKeyInputErrorRes = R.string.validation_key_already_exists
            return false
        }
        return true
    }

    fun onBackPressed() {
        if (hasChanges()) {
            showDiscardDialog()
        } else {
            finish()
        }
    }

    private fun hasChanges() =
        oldVariable?.isSameAs(variable) == false

    private fun showDiscardDialog() {
        emitEvent(
            ViewModelEvent.ShowDialog { context ->
                DialogBuilder(context)
                    .message(R.string.confirm_discard_changes_message)
                    .positive(R.string.dialog_discard) { onDiscardDialogConfirmed() }
                    .negative(R.string.dialog_cancel)
                    .showIfPossible()
            }
        )
    }

    private fun onDiscardDialogConfirmed() {
        finish()
    }

    fun onVariableKeyChanged(key: String) {
        updateVariableKey(key)
        performOperation(
            temporaryVariableRepository.setKey(key)
        )
    }

    private fun updateVariableKey(key: String) {
        variableKeyInputErrorRes = if (key.isEmpty() || Variables.isValidVariableKey(key)) {
            null
        } else {
            R.string.warning_invalid_variable_key
        }
    }

    fun onVariableTitleChanged(title: String) {
        performOperation(
            temporaryVariableRepository.setTitle(title)
        )
    }

    fun onUrlEncodeChanged(enabled: Boolean) {
        performOperation(
            temporaryVariableRepository.setUrlEncode(enabled)
        )
    }

    fun onJsonEncodeChanged(enabled: Boolean) {
        performOperation(
            temporaryVariableRepository.setJsonEncode(enabled)
        )
    }

    fun onAllowShareChanged(enabled: Boolean) {
        performOperation(
            temporaryVariableRepository.setShareText(enabled)
        )
    }

    data class InitData(
        val variableId: String?,
        val variableType: VariableType,
    )
}

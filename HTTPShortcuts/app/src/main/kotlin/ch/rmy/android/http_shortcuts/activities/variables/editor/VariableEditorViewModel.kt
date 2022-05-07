package ch.rmy.android.http_shortcuts.activities.variables.editor

import android.app.Application
import ch.rmy.android.framework.extensions.attachTo
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.utils.UUIDUtils.newUUID
import ch.rmy.android.framework.utils.localization.StringResLocalizable
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.framework.viewmodel.EventBridge
import ch.rmy.android.framework.viewmodel.WithDialog
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.variables.VariableTypeMappings
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.variables.TemporaryVariableRepository
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableId
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableKey
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableRepository
import ch.rmy.android.http_shortcuts.data.enums.VariableType
import ch.rmy.android.http_shortcuts.data.models.VariableModel
import ch.rmy.android.http_shortcuts.variables.Variables
import javax.inject.Inject

class VariableEditorViewModel(
    application: Application,
) : BaseViewModel<VariableEditorViewModel.InitData, VariableEditorViewState>(application), WithDialog {

    @Inject
    lateinit var variableRepository: VariableRepository

    @Inject
    lateinit var temporaryVariableRepository: TemporaryVariableRepository

    init {
        getApplicationComponent().inject(this)
    }

    private val outgoingEventBridge = EventBridge(VariableEditorToVariableTypeEvent::class.java)
    private val incomingEventBridge = EventBridge(VariableTypeToVariableEditorEvent::class.java)

    private val variableId: VariableId?
        get() = initData.variableId
    private val variableType: VariableType
        get() = initData.variableType

    private var oldVariable: VariableModel? = null
    private lateinit var variable: VariableModel
    private lateinit var variableKeysInUse: List<VariableKey>

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

    override var dialogState: DialogState?
        get() = currentViewState?.dialogState
        set(value) {
            updateViewState {
                copy(dialogState = value)
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
                        allowShareChecked = variable.isShareText || variable.isShareTitle,
                        shareSupport = variable.getShareSupport(),
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
        dialogState = DialogState.create {
            message(R.string.confirm_discard_changes_message)
                .positive(R.string.dialog_discard) { onDiscardDialogConfirmed() }
                .negative(R.string.dialog_cancel)
                .build()
        }
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
        doWithViewState { viewState ->
            performOperation(
                temporaryVariableRepository.setSharingSupport(
                    shareText = enabled && viewState.shareSupport.text,
                    shareTitle = enabled && viewState.shareSupport.title,
                )
            )
        }
    }

    fun onShareSupportChanged(shareSupport: VariableEditorViewState.ShareSupport) {
        performOperation(
            temporaryVariableRepository.setSharingSupport(
                shareText = shareSupport.text,
                shareTitle = shareSupport.title,
            )
        )
    }

    private fun VariableModel.getShareSupport(): VariableEditorViewState.ShareSupport {
        if (isShareTitle) {
            if (isShareText) {
                return VariableEditorViewState.ShareSupport.TITLE_AND_TEXT
            }
            return VariableEditorViewState.ShareSupport.TITLE
        }
        return VariableEditorViewState.ShareSupport.TEXT
    }

    data class InitData(
        val variableId: VariableId?,
        val variableType: VariableType,
    )
}

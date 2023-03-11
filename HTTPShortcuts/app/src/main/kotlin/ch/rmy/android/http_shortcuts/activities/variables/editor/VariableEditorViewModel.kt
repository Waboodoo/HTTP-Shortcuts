package ch.rmy.android.http_shortcuts.activities.variables.editor

import android.app.Application
import androidx.lifecycle.viewModelScope
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.utils.UUIDUtils.newUUID
import ch.rmy.android.framework.utils.localization.StringResLocalizable
import ch.rmy.android.framework.viewmodel.BaseViewModel
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
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.extensions.createDialogState
import ch.rmy.android.http_shortcuts.variables.Variables
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
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

    private val variableId: VariableId?
        get() = initData.variableId
    private val variableType: VariableType
        get() = initData.variableType

    private var oldVariable: Variable? = null
    private lateinit var variable: Variable
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
        viewModelScope.launch {
            try {
                if (data.variableId != null) {
                    variableRepository.createTemporaryVariableFromVariable(data.variableId)
                } else {
                    temporaryVariableRepository.createNewTemporaryVariable(variableType)
                }
                onTemporaryVariableCreated()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                handleInitializationError(e)
            }
        }

        viewModelScope.launch {
            variableRepository.getObservableVariables()
                .collect { variables ->
                    variableKeysInUse = variables
                        .filter { variable ->
                            variable.id != variableId
                        }
                        .map { variable ->
                            variable.key
                        }
                }
        }
    }

    private fun onTemporaryVariableCreated() {
        viewModelScope.launch {
            temporaryVariableRepository.getObservableTemporaryVariable()
                .collect { variable ->
                    this@VariableEditorViewModel.variable = variable
                    if (oldVariable == null) {
                        oldVariable = variable
                        finalizeInitialization(silent = true)
                    }
                    updateViewState {
                        copy(
                            variableKey = variable.key,
                            variableTitle = variable.title,
                            variableMessage = variable.message,
                            urlEncodeChecked = variable.urlEncode,
                            jsonEncodeChecked = variable.jsonEncode,
                            allowShareChecked = variable.isShareText || variable.isShareTitle,
                            shareSupport = variable.getShareSupport(),
                        )
                    }
                }
        }
    }

    private fun handleInitializationError(error: Throwable) {
        handleUnexpectedError(error)
        finish()
    }

    override fun initViewState() = VariableEditorViewState(
        title = StringResLocalizable(if (variableId == null) R.string.create_variable else R.string.edit_variable),
        subtitle = StringResLocalizable(VariableTypeMappings.getTypeName(variableType)),
        dialogTitleVisible = variableType.supportsDialogTitle,
        dialogMessageVisible = variableType.supportsDialogMessage,
    )

    fun onSaveButtonClicked() {
        trySave()
    }

    private fun trySave() {
        if (isSaving) {
            return
        }
        isSaving = true
        viewModelScope.launch {
            waitForOperationsToFinish()
            if (validate()) {
                if (variableType.hasFragment) {
                    emitEvent(VariableEditorEvent.Validate)
                } else {
                    onValidated(valid = true)
                }
            } else {
                isSaving = false
            }
        }
    }

    fun onValidated(valid: Boolean) {
        if (valid) {
            save()
        } else {
            isSaving = false
        }
    }

    private fun save() {
        viewModelScope.launch {
            try {
                variableRepository.copyTemporaryVariableToVariable(variableId ?: newUUID())
                finish()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                isSaving = false
                showSnackbar(R.string.error_generic)
                logException(e)
            }
        }
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
        dialogState = createDialogState {
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
        launchWithProgressTracking {
            temporaryVariableRepository.setKey(key)
        }
    }

    private fun updateVariableKey(key: String) {
        variableKeyInputErrorRes = if (key.isEmpty() || Variables.isValidVariableKey(key)) {
            null
        } else {
            R.string.warning_invalid_variable_key
        }
    }

    fun onVariableTitleChanged(title: String) {
        launchWithProgressTracking {
            temporaryVariableRepository.setTitle(title)
        }
    }

    fun onVariableMessageChanged(message: String) {
        launchWithProgressTracking {
            temporaryVariableRepository.setMessage(message)
        }
    }

    fun onUrlEncodeChanged(enabled: Boolean) {
        launchWithProgressTracking {
            temporaryVariableRepository.setUrlEncode(enabled)
        }
    }

    fun onJsonEncodeChanged(enabled: Boolean) {
        launchWithProgressTracking {
            temporaryVariableRepository.setJsonEncode(enabled)
        }
    }

    fun onAllowShareChanged(enabled: Boolean) {
        doWithViewState { viewState ->
            launchWithProgressTracking {
                temporaryVariableRepository.setSharingSupport(
                    shareText = enabled && viewState.shareSupport.text,
                    shareTitle = enabled && viewState.shareSupport.title,
                )
            }
        }
    }

    fun onShareSupportChanged(shareSupport: VariableEditorViewState.ShareSupport) {
        launchWithProgressTracking {
            temporaryVariableRepository.setSharingSupport(
                shareText = shareSupport.text,
                shareTitle = shareSupport.title,
            )
        }
    }

    private fun Variable.getShareSupport(): VariableEditorViewState.ShareSupport {
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

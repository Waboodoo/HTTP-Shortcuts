package ch.rmy.android.http_shortcuts.activities.variables.editor

import android.app.Application
import androidx.lifecycle.viewModelScope
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.utils.UUIDUtils.newUUID
import ch.rmy.android.framework.utils.localization.StringResLocalizable
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.variables.editor.models.ShareSupport
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.BaseTypeViewModel
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.ColorTypeViewModel
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.ConstantTypeViewModel
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.DateTypeViewModel
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.SelectTypeViewModel
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.SliderTypeViewModel
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.TextTypeViewModel
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.TimeTypeViewModel
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.ToggleTypeViewModel
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.VariableTypeViewState
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.variables.TemporaryVariableRepository
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableId
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableKey
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableRepository
import ch.rmy.android.http_shortcuts.data.enums.VariableType
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.variables.Variables
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import javax.inject.Inject

class VariableEditorViewModel(
    application: Application,
) : BaseViewModel<VariableEditorViewModel.InitData, VariableEditorViewState>(application) {

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

    private var typeViewModel: BaseTypeViewModel? = null

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

    override fun onInitializationStarted(data: InitData) {
        typeViewModel = when (data.variableType) {
            VariableType.COLOR -> ColorTypeViewModel()
            VariableType.CONSTANT -> ConstantTypeViewModel()
            VariableType.DATE -> DateTypeViewModel()
            VariableType.NUMBER,
            VariableType.PASSWORD,
            VariableType.TEXT,
            -> TextTypeViewModel()
            VariableType.SELECT -> SelectTypeViewModel()
            VariableType.SLIDER -> SliderTypeViewModel()
            VariableType.TIME -> TimeTypeViewModel()
            VariableType.TOGGLE -> ToggleTypeViewModel()
            else -> null
        }
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
                            dialogTitle = variable.title,
                            dialogMessage = variable.message,
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
        dialogTitleVisible = variableType.supportsDialogTitle,
        dialogMessageVisible = variableType.supportsDialogMessage,
        variableTypeViewState = typeViewModel?.createViewState(variable),
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
                save()
            } else {
                isSaving = false
            }
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
        val viewState = currentViewState ?: return false
        val variableKey = viewState.variableKey
        if (variableKey.isEmpty()) {
            variableKeyInputErrorRes = R.string.validation_key_non_empty
            return false
        }
        if (!Variables.isValidVariableKey(variableKey)) {
            variableKeyInputErrorRes = R.string.warning_invalid_variable_key
            return false
        }
        if (variableKeysInUse.contains(variableKey)) {
            variableKeyInputErrorRes = R.string.validation_key_already_exists
            return false
        }
        val newTypeViewState = viewState.variableTypeViewState?.let {
            typeViewModel?.validate(it)
        }
        if (newTypeViewState != null) {
            updateViewState {
                copy(variableTypeViewState = newTypeViewState)
            }
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
        updateDialogState(VariableEditorDialogState.DiscardWarning)
    }

    fun onDiscardDialogConfirmed() {
        finish()
    }

    fun onVariableKeyChanged(key: String) {
        updateVariableKey(key)
        launchWithProgressTracking {
            temporaryVariableRepository.setKey(key)
        }
    }

    private fun updateVariableKey(key: String) {
        updateViewState {
            copy(variableKey = key)
        }
        variableKeyInputErrorRes = if (key.isEmpty() || Variables.isValidVariableKey(key)) {
            null
        } else {
            R.string.warning_invalid_variable_key
        }
    }

    fun onDialogTitleChanged(title: String) {
        updateViewState {
            copy(dialogTitle = title)
        }
        launchWithProgressTracking {
            temporaryVariableRepository.setTitle(title)
        }
    }

    fun onDialogMessageChanged(message: String) {
        updateViewState {
            copy(dialogMessage = message)
        }
        launchWithProgressTracking {
            temporaryVariableRepository.setMessage(message)
        }
    }

    fun onUrlEncodeChanged(enabled: Boolean) {
        updateViewState {
            copy(urlEncodeChecked = enabled)
        }
        launchWithProgressTracking {
            temporaryVariableRepository.setUrlEncode(enabled)
        }
    }

    fun onJsonEncodeChanged(enabled: Boolean) {
        updateViewState {
            copy(jsonEncodeChecked = enabled)
        }
        launchWithProgressTracking {
            temporaryVariableRepository.setJsonEncode(enabled)
        }
    }

    fun onAllowShareChanged(enabled: Boolean) {
        updateViewState {
            copy(allowShareChecked = enabled)
        }
        doWithViewState { viewState ->
            launchWithProgressTracking {
                temporaryVariableRepository.setSharingSupport(
                    shareText = enabled && viewState.shareSupport.text,
                    shareTitle = enabled && viewState.shareSupport.title,
                )
            }
        }
    }

    fun onShareSupportChanged(shareSupport: ShareSupport) {
        updateViewState {
            copy(shareSupport = shareSupport)
        }
        launchWithProgressTracking {
            temporaryVariableRepository.setSharingSupport(
                shareText = shareSupport.text,
                shareTitle = shareSupport.title,
            )
        }
    }

    private fun Variable.getShareSupport(): ShareSupport {
        if (isShareTitle) {
            if (isShareText) {
                return ShareSupport.TITLE_AND_TEXT
            }
            return ShareSupport.TITLE
        }
        return ShareSupport.TEXT
    }

    fun onDismissDialog() {
        updateDialogState(null)
    }

    private fun updateDialogState(dialogState: VariableEditorDialogState?) {
        updateViewState {
            copy(dialogState = dialogState)
        }
    }

    fun onVariableTypeViewStateChanged(variableTypeViewState: VariableTypeViewState) {
        updateViewState {
            copy(variableTypeViewState = variableTypeViewState)
        }
        launchWithProgressTracking {
            typeViewModel?.save(temporaryVariableRepository, variableTypeViewState)
        }
    }

    data class InitData(
        val variableId: VariableId?,
        val variableType: VariableType,
    )
}

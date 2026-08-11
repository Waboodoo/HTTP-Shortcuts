package ch.rmy.android.http_shortcuts.activities.variables.editor

import android.app.Application
import androidx.lifecycle.viewModelScope
import ch.rmy.android.framework.utils.UUIDUtils.newUUID
import ch.rmy.android.framework.utils.localization.StringResLocalizable
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.variables.editor.models.ShareSupport
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.BaseTypeViewModel
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.ColorTypeViewModel
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.ConstantTypeViewModel
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.DateTypeViewModel
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.IncrementTypeViewModel
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.SelectTypeViewModel
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.SliderTypeViewModel
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.TextTypeViewModel
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.TimeTypeViewModel
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.TimestampTypeViewModel
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.ToggleTypeViewModel
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.VariableTypeViewState
import ch.rmy.android.http_shortcuts.data.domains.variables.GlobalVariableId
import ch.rmy.android.http_shortcuts.data.domains.variables.GlobalVariableRepository
import ch.rmy.android.http_shortcuts.data.domains.variables.TemporaryGlobalVariableRepository
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableKey
import ch.rmy.android.http_shortcuts.data.enums.VariableType
import ch.rmy.android.http_shortcuts.data.models.GlobalVariable
import ch.rmy.android.http_shortcuts.navigation.NavigationDestination.Companion.RESULT_CHANGES_DISCARDED
import ch.rmy.android.http_shortcuts.navigation.NavigationDestination.GlobalVariableEditor.VariableCreatedResult
import ch.rmy.android.http_shortcuts.utils.ExternalURLs
import ch.rmy.android.http_shortcuts.variables.Variables
import ch.rmy.android.http_shortcuts.widget.VariableWidgetManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@HiltViewModel
class GlobalVariableEditorViewModel
@Inject
constructor(
    application: Application,
    private val globalVariableRepository: GlobalVariableRepository,
    private val temporaryGlobalVariableRepository: TemporaryGlobalVariableRepository,
    private val variableWidgetManager: VariableWidgetManager,
) : BaseViewModel<GlobalVariableEditorViewModel.InitData, GlobalVariableEditorViewState>(application) {

    private val globalVariableId: GlobalVariableId?
        get() = initData.globalVariableId
    private val variableType: VariableType
        get() = initData.variableType

    private var typeViewModel: BaseTypeViewModel? = null

    private lateinit var globalVariable: GlobalVariable
    private lateinit var oldGlobalVariable: GlobalVariable
    private lateinit var variableKeysInUse: List<VariableKey>

    private var isSaving = false

    private var variableKeyInputErrorRes: Int? = null
        set(value) {
            if (field != value) {
                field = value
                viewModelScope.launch {
                    updateViewState {
                        copy(variableKeyInputError = value?.let { StringResLocalizable(it) })
                    }
                    if (value != null) {
                        emitEvent(GlobalVariableEditorEvent.FocusGlobalVariableKeyInput)
                    }
                }
            }
        }

    override suspend fun initialize(data: InitData): GlobalVariableEditorViewState {
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
            VariableType.INCREMENT -> IncrementTypeViewModel()
            VariableType.TIMESTAMP -> TimestampTypeViewModel()
            VariableType.UUID,
            VariableType.CLIPBOARD,
            -> null
        }
        if (data.globalVariableId != null) {
            globalVariableRepository.createTemporaryVariableFromVariable(data.globalVariableId)
        } else {
            temporaryGlobalVariableRepository.createNewTemporaryVariable(variableType)
        }

        viewModelScope.launch {
            globalVariableRepository.observeVariables()
                .collect { variables ->
                    variableKeysInUse = variables
                        .filter { variable ->
                            variable.id != globalVariableId
                        }
                        .map { variable ->
                            variable.key
                        }
                }
        }

        val variableFlow = temporaryGlobalVariableRepository.observeTemporaryVariable()
        globalVariable = variableFlow.first()
        oldGlobalVariable = globalVariable

        viewModelScope.launch {
            variableFlow.collect { variable ->
                this@GlobalVariableEditorViewModel.globalVariable = variable
                updateViewState {
                    copy(
                        variableKey = variable.key,
                        dialogTitle = variable.title,
                        dialogMessage = variable.message,
                        urlEncodeChecked = variable.urlEncode,
                        jsonEncodeChecked = variable.jsonEncode,
                        allowShareChecked = variable.isShareText || variable.isShareTitle,
                        shareSupport = variable.getShareSupport(),
                        excludeValueFromExports = variable.isExcludeValueFromExport,
                    )
                }
            }
        }
        return GlobalVariableEditorViewState(
            dialogTitleVisible = variableType.supportsDialogTitle,
            dialogMessageVisible = variableType.supportsDialogMessage,
            variableTypeViewState = typeViewModel?.createViewState(globalVariable),
            excludeValueCheckboxVisible = variableType.storesValue,
        )
    }

    fun onSaveButtonClicked() = runAction {
        trySave()
    }

    private suspend fun trySave() {
        if (isSaving) {
            return
        }
        isSaving = true
        waitForOperationsToFinish()
        if (validate()) {
            save()
        } else {
            isSaving = false
        }
    }

    private suspend fun save() {
        try {
            val id = globalVariableId ?: newUUID()
            globalVariableRepository.copyTemporaryVariableToVariable(id)
            globalVariableId?.let {
                variableWidgetManager.updateWidgets(it)
            }
            closeScreen(
                result = if (globalVariableId == null) {
                    VariableCreatedResult(
                        globalVariableId = id,
                    )
                } else {
                    null
                },
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            isSaving = false
            handleUnexpectedError(e)
        }
    }

    private suspend fun validate(): Boolean {
        val viewState = getCurrentViewState()
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

    fun onBackPressed() = runAction {
        if (hasChanges()) {
            showDiscardDialog()
        } else {
            closeScreen()
        }
    }

    private fun hasChanges() =
        globalVariable != oldGlobalVariable

    private suspend fun showDiscardDialog() {
        updateDialogState(GlobalVariableEditorDialogState.DiscardWarning)
    }

    fun onDiscardDialogConfirmed() = runAction {
        updateDialogState(null)
        closeScreen(result = RESULT_CHANGES_DISCARDED)
    }

    fun onVariableKeyChanged(key: String) = runAction {
        updateVariableKey(key)
        withProgressTracking {
            temporaryGlobalVariableRepository.setKey(key)
        }
    }

    private suspend fun updateVariableKey(key: String) {
        updateViewState {
            copy(variableKey = key)
        }
        variableKeyInputErrorRes = if (key.isEmpty() || Variables.isValidVariableKey(key)) {
            null
        } else {
            R.string.warning_invalid_variable_key
        }
    }

    fun onDialogTitleChanged(title: String) = runAction {
        updateViewState {
            copy(dialogTitle = title)
        }
        withProgressTracking {
            temporaryGlobalVariableRepository.setTitle(title)
        }
    }

    fun onDialogMessageChanged(message: String) = runAction {
        updateViewState {
            copy(dialogMessage = message)
        }
        withProgressTracking {
            temporaryGlobalVariableRepository.setMessage(message)
        }
    }

    fun onUrlEncodeChanged(enabled: Boolean) = runAction {
        updateViewState {
            copy(urlEncodeChecked = enabled)
        }
        withProgressTracking {
            temporaryGlobalVariableRepository.setUrlEncode(enabled)
        }
    }

    fun onJsonEncodeChanged(enabled: Boolean) = runAction {
        updateViewState {
            copy(jsonEncodeChecked = enabled)
        }
        withProgressTracking {
            temporaryGlobalVariableRepository.setJsonEncode(enabled)
        }
    }

    fun onAllowShareChanged(enabled: Boolean) = runAction {
        updateViewState {
            copy(allowShareChecked = enabled)
        }
        withProgressTracking {
            temporaryGlobalVariableRepository.setSharingSupport(
                shareText = enabled && viewState.shareSupport.text,
                shareTitle = enabled && viewState.shareSupport.title,
            )
        }
    }

    fun onShareSupportChanged(shareSupport: ShareSupport) = runAction {
        updateViewState {
            copy(shareSupport = shareSupport)
        }
        withProgressTracking {
            temporaryGlobalVariableRepository.setSharingSupport(
                shareText = shareSupport.text,
                shareTitle = shareSupport.title,
            )
        }
    }

    private fun GlobalVariable.getShareSupport(): ShareSupport {
        if (isShareTitle) {
            if (isShareText) {
                return ShareSupport.TITLE_AND_TEXT
            }
            return ShareSupport.TITLE
        }
        return ShareSupport.TEXT
    }

    fun onExcludeValueFromExportsChanged(exclude: Boolean) = runAction {
        updateViewState {
            copy(excludeValueFromExports = exclude)
        }
        withProgressTracking {
            temporaryGlobalVariableRepository.setExcludeValueFromExports(exclude)
        }
    }

    fun onDismissDialog() = runAction {
        updateDialogState(null)
    }

    private suspend fun updateDialogState(dialogState: GlobalVariableEditorDialogState?) {
        updateViewState {
            copy(dialogState = dialogState)
        }
    }

    fun onVariableTypeViewStateChanged(variableTypeViewState: VariableTypeViewState) = runAction {
        updateViewState {
            copy(variableTypeViewState = variableTypeViewState)
        }
        withProgressTracking {
            typeViewModel?.save(temporaryGlobalVariableRepository, variableTypeViewState)
        }
    }

    fun onHelpButtonClicked() = runAction {
        openURL(ExternalURLs.getVariableTypeDocumentation(variableType))
    }

    data class InitData(
        val globalVariableId: GlobalVariableId?,
        val variableType: VariableType,
    )
}

package ch.rmy.android.http_shortcuts.activities.editor

import android.app.Application
import androidx.lifecycle.viewModelScope
import ch.rmy.android.framework.extensions.context
import ch.rmy.android.framework.extensions.logInfo
import ch.rmy.android.framework.extensions.toLocalizable
import ch.rmy.android.framework.utils.UUIDUtils.newUUID
import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.framework.utils.localization.QuantityStringLocalizable
import ch.rmy.android.framework.utils.localization.StringResLocalizable
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.framework.viewmodel.ViewModelScope
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.editor.usecases.FetchFaviconUseCase
import ch.rmy.android.http_shortcuts.activities.execute.ExecuteDialogHandler
import ch.rmy.android.http_shortcuts.activities.execute.ExecuteDialogState
import ch.rmy.android.http_shortcuts.activities.execute.ExecutionStarter
import ch.rmy.android.http_shortcuts.data.SessionInfoStore
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.TemporaryShortcutRepository
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableRepository
import ch.rmy.android.http_shortcuts.data.enums.FileUploadType
import ch.rmy.android.http_shortcuts.data.enums.RequestBodyType
import ch.rmy.android.http_shortcuts.data.enums.ShortcutAuthenticationType
import ch.rmy.android.http_shortcuts.data.enums.ShortcutExecutionType
import ch.rmy.android.http_shortcuts.data.enums.ShortcutTriggerType
import ch.rmy.android.http_shortcuts.data.maintenance.CleanUpWorker
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.data.models.Shortcut.Companion.TEMPORARY_ID
import ch.rmy.android.http_shortcuts.extensions.type
import ch.rmy.android.http_shortcuts.icons.Icons
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon
import ch.rmy.android.http_shortcuts.navigation.NavigationArgStore
import ch.rmy.android.http_shortcuts.navigation.NavigationDestination
import ch.rmy.android.http_shortcuts.scripting.shortcuts.TriggerShortcutManager
import ch.rmy.android.http_shortcuts.utils.LauncherShortcutUpdater
import ch.rmy.android.http_shortcuts.utils.Validation.isAcceptableHttpUrl
import ch.rmy.android.http_shortcuts.utils.Validation.isAcceptableUrl
import ch.rmy.android.http_shortcuts.widget.WidgetManager
import ch.rmy.curlcommand.CurlCommand
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShortcutEditorViewModel
@Inject
constructor(
    application: Application,
    private val shortcutRepository: ShortcutRepository,
    private val temporaryShortcutRepository: TemporaryShortcutRepository,
    private val variableRepository: VariableRepository,
    private val widgetManager: WidgetManager,
    private val fetchFavicon: FetchFaviconUseCase,
    private val sessionInfoStore: SessionInfoStore,
    private val cleanUpStarter: CleanUpWorker.Starter,
    private val dialogHandler: ExecuteDialogHandler,
    private val launcherShortcutUpdater: LauncherShortcutUpdater,
    private val executionStarter: ExecutionStarter,
    private val navigationArgStore: NavigationArgStore,
) : BaseViewModel<ShortcutEditorViewModel.InitData, ShortcutEditorViewState>(application) {

    private var isSaving = false
        set(value) {
            if (field != value) {
                field = value
                viewModelScope.launch {
                    updateViewState {
                        copy(isInputDisabled = value)
                    }
                }
            }
        }

    private var isFinishing = false

    private var oldShortcut: Shortcut? = null

    private lateinit var shortcut: Shortcut

    private val categoryId
        get() = initData.categoryId

    private val shortcutId
        get() = initData.shortcutId

    private val executionType
        get() = initData.executionType

    override suspend fun initialize(data: InitData): ShortcutEditorViewState {
        sessionInfoStore.editingShortcutId = data.shortcutId
        sessionInfoStore.editingShortcutCategoryId = data.categoryId

        when {
            data.recoveryMode -> Unit
            data.shortcutId == null -> {
                temporaryShortcutRepository.createNewTemporaryShortcut(
                    initialIcon = Icons.getRandomInitialIcon(context),
                    executionType = executionType,
                    categoryId = data.categoryId,
                )
            }
            else -> {
                shortcutRepository.createTemporaryShortcutFromShortcut(data.shortcutId, data.categoryId)
            }
        }
        data.curlCommandId
            ?.let {
                navigationArgStore.takeArg(it) as CurlCommand?
            }
            ?.let { curlCommand ->
                temporaryShortcutRepository.importFromCurl(curlCommand)
            }

        val shortcutFlow = temporaryShortcutRepository.getObservableTemporaryShortcut()
        this.shortcut = shortcutFlow.first()
        oldShortcut = this.shortcut

        viewModelScope.launch {
            shortcutFlow
                .collect { shortcut ->
                    this@ShortcutEditorViewModel.shortcut = shortcut
                    updateViewState {
                        copy(
                            toolbarSubtitle = getToolbarSubtitle(),
                            shortcutExecutionType = shortcut.type,
                            shortcutIcon = shortcut.icon,
                            shortcutName = shortcut.name,
                            shortcutDescription = shortcut.description,
                            isExecutable = canExecute(),
                            hasChanges = hasChanges(),
                            requestBodyButtonEnabled = shortcut.allowsBody(),
                            basicSettingsSubtitle = getBasicSettingsSubtitle(),
                            headersSubtitle = getHeadersSubtitle(),
                            requestBodySubtitle = getRequestBodySubtitle(),
                            authenticationSettingsSubtitle = getAuthenticationSubtitle(),
                            scriptingSubtitle = getScriptingSubtitle(),
                            triggerShortcutsSubtitle = getTriggerShortcutsSubtitle(),
                        )
                    }
                }
        }
        return ShortcutEditorViewState(
            shortcutExecutionType = executionType,
            toolbarSubtitle = getToolbarSubtitle(),
            shortcutIcon = shortcut.icon,
            shortcutName = shortcut.name,
            shortcutDescription = shortcut.description,
            isExecutable = canExecute(),
            hasChanges = hasChanges(),
            requestBodyButtonEnabled = shortcut.allowsBody(),
            basicSettingsSubtitle = getBasicSettingsSubtitle(),
            headersSubtitle = getHeadersSubtitle(),
            requestBodySubtitle = getRequestBodySubtitle(),
            authenticationSettingsSubtitle = getAuthenticationSubtitle(),
            scriptingSubtitle = getScriptingSubtitle(),
            triggerShortcutsSubtitle = getTriggerShortcutsSubtitle(),
        )
    }

    private fun hasChanges() =
        initData.recoveryMode || oldShortcut?.isSameAs(shortcut) == false || initData.curlCommandId != null

    private fun canExecute() =
        when (shortcut.type) {
            ShortcutExecutionType.APP -> isAcceptableHttpUrl(shortcut.url)
            ShortcutExecutionType.BROWSER -> isAcceptableUrl(shortcut.url)
            ShortcutExecutionType.SCRIPTING,
            ShortcutExecutionType.TRIGGER,
            -> shortcut.codeOnPrepare.isNotEmpty()
        }

    private fun getToolbarSubtitle() =
        when (shortcut.type) {
            ShortcutExecutionType.BROWSER -> StringResLocalizable(R.string.subtitle_editor_toolbar_browser_shortcut)
            ShortcutExecutionType.SCRIPTING -> StringResLocalizable(R.string.subtitle_editor_toolbar_scripting_shortcut)
            ShortcutExecutionType.TRIGGER -> StringResLocalizable(R.string.subtitle_editor_toolbar_trigger_shortcut)
            else -> null
        }

    private fun getBasicSettingsSubtitle(): Localizable =
        if (shortcut.type == ShortcutExecutionType.BROWSER) {
            if (!hasUrl()) {
                StringResLocalizable(R.string.subtitle_basic_request_settings_url_only_prompt)
            } else {
                shortcut.url.toLocalizable()
            }
        } else {
            if (!hasUrl()) {
                StringResLocalizable(R.string.subtitle_basic_request_settings_prompt)
            } else {
                StringResLocalizable(
                    R.string.subtitle_basic_request_settings_pattern,
                    shortcut.method,
                    shortcut.url,
                )
            }
        }

    private fun hasUrl() =
        shortcut.url.let { it.isNotEmpty() && it != "http://" && it != "https://" }

    private fun getHeadersSubtitle(): Localizable {
        val count = shortcut.headers.size
        return if (count == 0) {
            StringResLocalizable(R.string.subtitle_request_headers_none)
        } else {
            QuantityStringLocalizable(R.plurals.subtitle_request_headers_pattern, count)
        }
    }

    private fun getRequestBodySubtitle(): Localizable =
        if (shortcut.allowsBody()) {
            when (shortcut.bodyType) {
                RequestBodyType.FORM_DATA,
                RequestBodyType.X_WWW_FORM_URLENCODE,
                -> {
                    val count = shortcut.parameters.size
                    if (count == 0) {
                        StringResLocalizable(R.string.subtitle_request_body_params_none)
                    } else {
                        QuantityStringLocalizable(R.plurals.subtitle_request_body_params_pattern, count)
                    }
                }
                RequestBodyType.FILE -> {
                    if (shortcut.fileUploadOptions?.type == FileUploadType.CAMERA) {
                        StringResLocalizable(R.string.subtitle_request_body_image)
                    } else {
                        StringResLocalizable(R.string.subtitle_request_body_file)
                    }
                }
                RequestBodyType.CUSTOM_TEXT -> if (shortcut.bodyContent.isBlank()) {
                    StringResLocalizable(R.string.subtitle_request_body_none)
                } else {
                    StringResLocalizable(
                        R.string.subtitle_request_body_custom,
                        shortcut.contentType
                            .ifEmpty { Shortcut.DEFAULT_CONTENT_TYPE },
                    )
                }
            }
        } else {
            StringResLocalizable(R.string.subtitle_request_body_not_available, shortcut.method)
        }

    private fun getAuthenticationSubtitle(): Localizable =
        StringResLocalizable(
            when (shortcut.authenticationType) {
                ShortcutAuthenticationType.BASIC -> R.string.subtitle_authentication_basic
                ShortcutAuthenticationType.DIGEST -> R.string.subtitle_authentication_digest
                ShortcutAuthenticationType.BEARER -> R.string.subtitle_authentication_bearer
                ShortcutAuthenticationType.NONE -> if (shortcut.clientCertParams != null && !shortcut.acceptAllCertificates) {
                    R.string.subtitle_authentication_client_cert
                } else {
                    R.string.subtitle_authentication_none
                }
            }
        )

    private fun getScriptingSubtitle(): Localizable =
        StringResLocalizable(
            when (shortcut.type) {
                ShortcutExecutionType.SCRIPTING -> R.string.label_scripting_scripting_shortcuts_subtitle
                ShortcutExecutionType.BROWSER -> R.string.label_scripting_browser_shortcuts_subtitle
                else -> R.string.label_scripting_subtitle
            }
        )

    private fun getTriggerShortcutsSubtitle(): Localizable {
        if (shortcut.type != ShortcutExecutionType.TRIGGER) {
            return Localizable.EMPTY
        }
        val count = TriggerShortcutManager.getTriggeredShortcutIdsFromCode(shortcut.codeOnPrepare).size
        return if (count == 0) {
            StringResLocalizable(R.string.label_trigger_shortcuts_subtitle_none)
        } else {
            QuantityStringLocalizable(R.plurals.label_trigger_shortcuts_subtitle, count)
        }
    }

    fun onShortcutIconChanged(icon: ShortcutIcon) = runAction {
        skipIfBusy()
        updateViewState {
            copy(
                dialogState = null,
                shortcutIcon = icon,
            )
        }
        withProgressTracking {
            temporaryShortcutRepository.setIcon(icon)
        }
    }

    fun onShortcutNameChanged(name: String) = runAction {
        skipIfBusy()
        updateViewState {
            copy(shortcutName = name)
        }
        withProgressTracking {
            temporaryShortcutRepository.setName(name)
        }
    }

    fun onShortcutDescriptionChanged(description: String) = runAction {
        skipIfBusy()
        updateViewState {
            copy(shortcutDescription = description)
        }
        withProgressTracking {
            temporaryShortcutRepository.setDescription(description)
        }
    }

    fun onTestButtonClicked() = runAction {
        skipIfBusy()
        if (!viewState.isExecutable) {
            skipAction()
        }
        logInfo("Test button clicked")
        waitForOperationsToFinish()
        executionStarter.execute(
            shortcutId = TEMPORARY_ID,
            trigger = ShortcutTriggerType.TEST_IN_EDITOR,
        )
    }

    fun onSaveButtonClicked() = runAction {
        skipIfBusy()
        logInfo("Save button clicked")
        if (!viewState.hasChanges) {
            skipAction()
        }
        isSaving = true
        waitForOperationsToFinish()
        trySave()
    }

    private suspend fun ViewModelScope<*>.trySave() {
        if (shortcut.name.isBlank()) {
            showSnackbar(R.string.validation_name_not_empty, long = true)
            emitEvent(ShortcutEditorEvent.FocusNameInputField)
            isSaving = false
            return
        }
        if (
            (shortcut.type.requiresHttpUrl && !isAcceptableHttpUrl(shortcut.url)) ||
            (shortcut.type.usesUrl && !shortcut.type.requiresHttpUrl && !isAcceptableUrl(shortcut.url))
        ) {
            showSnackbar(R.string.validation_url_invalid, long = true)
            isSaving = false
            return
        }

        save()
    }

    private suspend fun ViewModelScope<*>.save() {
        logInfo("Beginning saving changes to shortcut")
        val isNewShortcut = shortcutId == null
        val shortcutId = shortcutId ?: newUUID()

        try {
            withProgressTracking {
                shortcutRepository.copyTemporaryShortcutToShortcut(shortcutId, categoryId.takeIf { isNewShortcut })
                temporaryShortcutRepository.deleteTemporaryShortcut()
            }
            onSaveSuccessful(shortcutId)
        } catch (e: Exception) {
            isSaving = false
            throw e
        }
    }

    private suspend fun ViewModelScope<*>.onSaveSuccessful(shortcutId: ShortcutId) {
        logInfo("Shortcut saved successfully")
        isFinishing = true
        launcherShortcutUpdater.updatePinnedShortcut(shortcutId)
        withProgressTracking {
            widgetManager.updateWidgets(context, shortcutId)
        }
        waitForOperationsToFinish()
        cleanUpStarter()
        closeScreen(result = NavigationDestination.ShortcutEditor.ShortcutCreatedResult(shortcutId))
    }

    fun onBackPressed() = runAction {
        skipIfBusy()
        waitForOperationsToFinish()
        if (hasChanges()) {
            showDiscardDialog()
        } else {
            onDiscardDialogConfirmed()
        }
    }

    private suspend fun showDiscardDialog() {
        updateDialogState(ShortcutEditorDialogState.DiscardWarning)
    }

    fun onDiscardDialogConfirmed() = runAction {
        updateDialogState(null)
        logInfo("Beginning discarding changes to shortcut")
        isFinishing = true
        withProgressTracking {
            temporaryShortcutRepository.deleteTemporaryShortcut()
        }
        logInfo("Changes to shortcut discarded")
        waitForOperationsToFinish()
        cleanUpStarter()
        closeScreen()
    }

    fun onBasicRequestSettingsButtonClicked() = runAction {
        skipIfBusy()
        logInfo("Basic request settings button clicked")
        navigate(NavigationDestination.ShortcutEditorBasicRequestSettings)
    }

    fun onHeadersButtonClicked() = runAction {
        skipIfBusy()
        logInfo("Headers settings button clicked")
        navigate(NavigationDestination.ShortcutEditorRequestHeaders)
    }

    fun onRequestBodyButtonClicked() = runAction {
        skipIfBusy()
        logInfo("Request body settings button clicked")
        navigate(NavigationDestination.ShortcutEditorRequestBody)
    }

    fun onAuthenticationButtonClicked() = runAction {
        skipIfBusy()
        logInfo("Authentication settings button clicked")
        navigate(NavigationDestination.ShortcutEditorAuthentication)
    }

    fun onResponseHandlingButtonClicked() = runAction {
        skipIfBusy()
        logInfo("Response handling button clicked")
        navigate(NavigationDestination.ShortcutEditorResponse)
    }

    fun onScriptingButtonClicked() = runAction {
        skipIfBusy()
        logInfo("Scripting button clicked")
        navigate(NavigationDestination.ShortcutEditorScripting.buildRequest(shortcutId))
    }

    fun onTriggerShortcutsButtonClicked() = runAction {
        skipIfBusy()
        logInfo("Trigger shortcuts button clicked")
        navigate(NavigationDestination.ShortcutEditorTriggerShortcuts.buildRequest(shortcutId))
    }

    fun onExecutionSettingsButtonClicked() = runAction {
        skipIfBusy()
        logInfo("Execution settings button clicked")
        navigate(NavigationDestination.ShortcutEditorExecutionSettings)
    }

    fun onAdvancedSettingsButtonClicked() = runAction {
        skipIfBusy()
        logInfo("Advanced settings button clicked")
        navigate(NavigationDestination.ShortcutEditorAdvancedSettings)
    }

    fun onIconClicked() = runAction {
        skipIfBusy()
        logInfo("Icon clicked")
        updateDialogState(
            ShortcutEditorDialogState.PickIcon(
                currentIcon = viewState.shortcutIcon as? ShortcutIcon.BuiltInIcon,
                suggestionBase = viewState.shortcutName,
                includeFaviconOption = hasUrl(),
            ),
        )
    }

    fun onFetchFaviconOptionSelected() = runAction {
        skipIfBusy()
        logInfo("Fetching favicon")
        updateViewState {
            copy(
                iconLoading = true,
                dialogState = null,
            )
        }
        try {
            val variables = variableRepository.getVariables()
            val icon = fetchFavicon(shortcut.url, variables, dialogHandler)
            if (icon != null) {
                onShortcutIconChanged(icon)
            } else {
                showSnackbar(R.string.error_failed_to_fetch_favicon)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleUnexpectedError(e)
        } finally {
            updateViewState {
                copy(iconLoading = false)
            }
        }
    }

    fun onDismissDialog() = runAction {
        updateDialogState(null)
    }

    private suspend fun updateDialogState(dialogState: ShortcutEditorDialogState?) {
        updateViewState {
            copy(dialogState = dialogState)
        }
    }

    val executeDialogState: StateFlow<ExecuteDialogState<*>?>
        get() = dialogHandler.dialogState

    fun onExecuteDialogDismissed() {
        dialogHandler.onDialogDismissed()
    }

    fun onExecuteDialogResult(result: Any) {
        dialogHandler.onDialogResult(result)
    }

    private fun ViewModelScope<*>.skipIfBusy() {
        if (isSaving || isFinishing) {
            skipAction()
        }
    }

    fun onCustomIconOptionSelected() = runAction {
        updateDialogState(null)
        navigate(NavigationDestination.IconPicker)
    }

    data class InitData(
        val categoryId: CategoryId,
        val shortcutId: ShortcutId?,
        val curlCommandId: NavigationArgStore.ArgStoreId?,
        val executionType: ShortcutExecutionType,
        val recoveryMode: Boolean,
    )
}

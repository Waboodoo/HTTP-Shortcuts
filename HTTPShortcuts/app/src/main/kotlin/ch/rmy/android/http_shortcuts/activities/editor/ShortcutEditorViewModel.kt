package ch.rmy.android.http_shortcuts.activities.editor

import android.app.Activity
import android.app.Application
import android.content.Intent
import androidx.lifecycle.viewModelScope
import ch.rmy.android.framework.extensions.context
import ch.rmy.android.framework.extensions.logInfo
import ch.rmy.android.framework.extensions.toLocalizable
import ch.rmy.android.framework.utils.UUIDUtils.newUUID
import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.framework.utils.localization.QuantityStringLocalizable
import ch.rmy.android.framework.utils.localization.StringResLocalizable
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.ExecuteActivity
import ch.rmy.android.http_shortcuts.activities.editor.advancedsettings.AdvancedSettingsActivity
import ch.rmy.android.http_shortcuts.activities.editor.authentication.AuthenticationActivity
import ch.rmy.android.http_shortcuts.activities.editor.basicsettings.BasicRequestSettingsActivity
import ch.rmy.android.http_shortcuts.activities.editor.body.RequestBodyActivity
import ch.rmy.android.http_shortcuts.activities.editor.executionsettings.ExecutionSettingsActivity
import ch.rmy.android.http_shortcuts.activities.editor.headers.RequestHeadersActivity
import ch.rmy.android.http_shortcuts.activities.editor.response.ResponseActivity
import ch.rmy.android.http_shortcuts.activities.editor.scripting.ScriptingActivity
import ch.rmy.android.http_shortcuts.activities.editor.shortcuts.TriggerShortcutsActivity
import ch.rmy.android.http_shortcuts.activities.editor.usecases.FetchFaviconUseCase
import ch.rmy.android.http_shortcuts.activities.execute.ExecuteDialogHandler
import ch.rmy.android.http_shortcuts.activities.execute.ExecuteDialogState
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
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
import ch.rmy.android.http_shortcuts.scripting.shortcuts.TriggerShortcutManager
import ch.rmy.android.http_shortcuts.utils.LauncherShortcutManager
import ch.rmy.android.http_shortcuts.utils.Validation.isAcceptableHttpUrl
import ch.rmy.android.http_shortcuts.utils.Validation.isAcceptableUrl
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
import ch.rmy.android.http_shortcuts.widget.WidgetManager
import ch.rmy.curlcommand.CurlCommand
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class ShortcutEditorViewModel(
    application: Application,
) : BaseViewModel<ShortcutEditorViewModel.InitData, ShortcutEditorViewState>(application) {

    @Inject
    lateinit var shortcutRepository: ShortcutRepository

    @Inject
    lateinit var temporaryShortcutRepository: TemporaryShortcutRepository

    @Inject
    lateinit var variableRepository: VariableRepository

    @Inject
    lateinit var widgetManager: WidgetManager

    @Inject
    lateinit var fetchFavicon: FetchFaviconUseCase

    @Inject
    lateinit var launcherShortcutManager: LauncherShortcutManager

    @Inject
    lateinit var sessionInfoStore: SessionInfoStore

    @Inject
    lateinit var variablePlaceholderProvider: VariablePlaceholderProvider

    @Inject
    lateinit var cleanUpStarter: CleanUpWorker.Starter

    @Inject
    lateinit var dialogHandler: ExecuteDialogHandler

    init {
        getApplicationComponent().inject(this)
    }

    private var isSaving = false
        set(value) {
            if (field != value) {
                field = value
                updateViewState {
                    copy(isInputDisabled = value)
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

    override fun onInitializationStarted(data: InitData) {
        sessionInfoStore.editingShortcutId = data.shortcutId
        sessionInfoStore.editingShortcutCategoryId = data.categoryId
        viewModelScope.launch {
            try {
                when {
                    data.recoveryMode -> Unit
                    data.shortcutId == null -> {
                        temporaryShortcutRepository.createNewTemporaryShortcut(
                            initialIcon = Icons.getRandomInitialIcon(context),
                            executionType = executionType,
                        )
                    }
                    else -> {
                        shortcutRepository.createTemporaryShortcutFromShortcut(data.shortcutId)
                    }
                }
                data.curlCommand?.let {
                    temporaryShortcutRepository.importFromCurl(it)
                }
                observeTemporaryShortcut()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                finish()
                handleUnexpectedError(e)
            }
        }
    }

    override fun initViewState() = ShortcutEditorViewState(
        shortcutExecutionType = executionType,
    )

    private fun observeTemporaryShortcut() {
        viewModelScope.launch {
            temporaryShortcutRepository.getObservableTemporaryShortcut()
                .collect { shortcut ->
                    this@ShortcutEditorViewModel.shortcut = shortcut
                    if (!isInitialized) {
                        oldShortcut = shortcut
                        finalizeInitialization(silent = true)
                    }
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
    }

    private fun hasChanges() =
        initData.recoveryMode || oldShortcut?.isSameAs(shortcut) == false || initData.curlCommand != null

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

    fun onShortcutIconChanged(icon: ShortcutIcon) {
        if (isSaving || isFinishing) {
            return
        }
        updateViewState {
            copy(
                dialogState = null,
                shortcutIcon = icon,
            )
        }
        doWithViewState {
            launchWithProgressTracking {
                temporaryShortcutRepository.setIcon(icon)
            }
        }
    }

    fun onShortcutNameChanged(name: String) {
        if (!isInitialized || isSaving || isFinishing) {
            return
        }
        updateViewState {
            copy(shortcutName = name)
        }
        viewModelScope.launch {
            withProgressTracking {
                temporaryShortcutRepository.setName(name)
            }
        }
    }

    fun onShortcutDescriptionChanged(description: String) {
        if (!isInitialized || isSaving || isFinishing) {
            return
        }
        updateViewState {
            copy(shortcutDescription = description)
        }
        launchWithProgressTracking {
            temporaryShortcutRepository.setDescription(description)
        }
    }

    fun onTestButtonClicked() {
        if (isSaving || isFinishing) {
            return
        }
        logInfo("Test button clicked")
        doWithViewState { viewState ->
            if (!viewState.isExecutable) {
                return@doWithViewState
            }
            viewModelScope.launch {
                waitForOperationsToFinish()
                openActivity(ExecuteActivity.IntentBuilder(TEMPORARY_ID).trigger(ShortcutTriggerType.TEST_IN_EDITOR))
            }
        }
    }

    fun onSaveButtonClicked() {
        logInfo("Save button clicked")
        if (isSaving || isFinishing) {
            return
        }
        doWithViewState { viewState ->
            if (!viewState.hasChanges || isSaving) {
                return@doWithViewState
            }
            isSaving = true
            viewModelScope.launch {
                waitForOperationsToFinish()
                trySave()
            }
        }
    }

    private fun trySave() {
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

    private fun save() {
        logInfo("Beginning saving changes to shortcut")
        val isNewShortcut = shortcutId == null
        val shortcutId = shortcutId ?: newUUID()

        viewModelScope.launch {
            try {
                withProgressTracking {
                    shortcutRepository.copyTemporaryShortcutToShortcut(shortcutId, categoryId?.takeIf { isNewShortcut })
                    temporaryShortcutRepository.deleteTemporaryShortcut()
                }
                onSaveSuccessful(shortcutId)
            } catch (e: Exception) {
                isSaving = false
                throw e
            }
        }
    }

    private fun onSaveSuccessful(shortcutId: ShortcutId) {
        logInfo("Shortcut saved successfully")
        launcherShortcutManager.updatePinnedShortcut(shortcutId, shortcut.name, shortcut.icon)
        viewModelScope.launch {
            withProgressTracking {
                widgetManager.updateWidgets(context, shortcutId)
            }
            viewModelScope.launch {
                waitForOperationsToFinish()
                cleanUpStarter()
                finishWithOkResult(
                    ShortcutEditorActivity.OpenShortcutEditor.createResult(shortcutId),
                )
            }
        }
    }

    fun onBackPressed() {
        if (isSaving || isFinishing) {
            return
        }
        viewModelScope.launch {
            waitForOperationsToFinish()
            if (hasChanges()) {
                showDiscardDialog()
            } else {
                onDiscardDialogConfirmed()
            }
        }
    }

    private fun showDiscardDialog() {
        updateDialogState(ShortcutEditorDialogState.DiscardWarning)
    }

    fun onDiscardDialogConfirmed() {
        updateDialogState(null)
        logInfo("Beginning discarding changes to shortcut")
        isFinishing = true
        viewModelScope.launch {
            withProgressTracking {
                temporaryShortcutRepository.deleteTemporaryShortcut()
            }
            logInfo("Changes to shortcut discarded")
            viewModelScope.launch {
                waitForOperationsToFinish()
                cleanUpStarter()
                finish(result = Activity.RESULT_CANCELED)
            }
        }
    }

    fun onBasicRequestSettingsButtonClicked() {
        if (isSaving || isFinishing) {
            return
        }
        logInfo("Basic request settings button clicked")
        openActivity(BasicRequestSettingsActivity.IntentBuilder())
    }

    fun onHeadersButtonClicked() {
        if (isSaving || isFinishing) {
            return
        }
        logInfo("Headers settings button clicked")
        openActivity(RequestHeadersActivity.IntentBuilder())
    }

    fun onRequestBodyButtonClicked() {
        if (isSaving || isFinishing) {
            return
        }
        logInfo("Request body settings button clicked")
        openActivity(RequestBodyActivity.IntentBuilder())
    }

    fun onAuthenticationButtonClicked() {
        if (isSaving || isFinishing) {
            return
        }
        logInfo("Authentication settings button clicked")
        openActivity(AuthenticationActivity.IntentBuilder())
    }

    fun onResponseHandlingButtonClicked() {
        if (isSaving || isFinishing) {
            return
        }
        logInfo("Response handling button clicked")
        openActivity(ResponseActivity.IntentBuilder())
    }

    fun onScriptingButtonClicked() {
        if (isSaving || isFinishing) {
            return
        }
        logInfo("Scripting button clicked")
        openActivity(
            ScriptingActivity.IntentBuilder()
                .shortcutId(shortcutId)
        )
    }

    fun onTriggerShortcutsButtonClicked() {
        if (isSaving || isFinishing) {
            return
        }
        logInfo("Trigger shortcuts button clicked")
        openActivity(
            TriggerShortcutsActivity.IntentBuilder()
                .shortcutId(shortcutId)
        )
    }

    fun onExecutionSettingsButtonClicked() {
        if (isSaving || isFinishing) {
            return
        }
        logInfo("Execution settings button clicked")
        openActivity(ExecutionSettingsActivity.IntentBuilder())
    }

    fun onAdvancedSettingsButtonClicked() {
        if (isSaving || isFinishing) {
            return
        }
        logInfo("Advanced settings button clicked")
        openActivity(AdvancedSettingsActivity.IntentBuilder())
    }

    fun onIconClicked() {
        if (isSaving || isFinishing) {
            return
        }
        logInfo("Icon clicked")
        updateDialogState(
            ShortcutEditorDialogState.PickIcon(
                currentIcon = currentViewState?.shortcutIcon as? ShortcutIcon.BuiltInIcon,
                includeFaviconOption = hasUrl(),
            ),
        )
    }

    fun onFetchFaviconOptionSelected() {
        updateDialogState(null)
        if (isSaving) {
            return
        }
        logInfo("Fetching favicon")
        viewModelScope.launch {
            updateViewState {
                copy(iconLoading = true)
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
    }

    override fun finish(result: Int?, intent: Intent?, skipAnimation: Boolean) {
        isFinishing = true
        super.finish(result, intent, skipAnimation)
    }

    fun onDismissDialog() {
        updateDialogState(null)
    }

    private fun updateDialogState(dialogState: ShortcutEditorDialogState?) {
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

    data class InitData(
        val categoryId: CategoryId?,
        val shortcutId: ShortcutId?,
        val curlCommand: CurlCommand?,
        val executionType: ShortcutExecutionType,
        val recoveryMode: Boolean,
    )
}

package ch.rmy.android.http_shortcuts.activities.editor

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.app.Application
import android.content.Intent
import ch.rmy.android.framework.extensions.attachTo
import ch.rmy.android.framework.extensions.color
import ch.rmy.android.framework.extensions.context
import ch.rmy.android.framework.extensions.mapIfNotNull
import ch.rmy.android.framework.extensions.toLocalizable
import ch.rmy.android.framework.utils.UUIDUtils.newUUID
import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.framework.utils.localization.QuantityStringLocalizable
import ch.rmy.android.framework.utils.localization.StringResLocalizable
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.framework.viewmodel.ViewModelEvent
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.ExecuteActivity
import ch.rmy.android.http_shortcuts.activities.editor.ShortcutEditorActivity.Companion.RESULT_SHORTCUT_ID
import ch.rmy.android.http_shortcuts.activities.editor.advancedsettings.AdvancedSettingsActivity
import ch.rmy.android.http_shortcuts.activities.editor.authentication.AuthenticationActivity
import ch.rmy.android.http_shortcuts.activities.editor.basicsettings.BasicRequestSettingsActivity
import ch.rmy.android.http_shortcuts.activities.editor.body.RequestBodyActivity
import ch.rmy.android.http_shortcuts.activities.editor.executionsettings.ExecutionSettingsActivity
import ch.rmy.android.http_shortcuts.activities.editor.headers.RequestHeadersActivity
import ch.rmy.android.http_shortcuts.activities.editor.response.ResponseActivity
import ch.rmy.android.http_shortcuts.activities.editor.scripting.ScriptingActivity
import ch.rmy.android.http_shortcuts.activities.editor.shortcuts.TriggerShortcutsActivity
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.TemporaryShortcutRepository
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableRepository
import ch.rmy.android.http_shortcuts.data.enums.RequestBodyType
import ch.rmy.android.http_shortcuts.data.enums.ShortcutExecutionType
import ch.rmy.android.http_shortcuts.data.maintenance.CleanUpWorker
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.data.models.Shortcut.Companion.TEMPORARY_ID
import ch.rmy.android.http_shortcuts.dialogs.DialogBuilder
import ch.rmy.android.http_shortcuts.extensions.type
import ch.rmy.android.http_shortcuts.icons.Icons
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon
import ch.rmy.android.http_shortcuts.scripting.shortcuts.TriggerShortcutManager
import ch.rmy.android.http_shortcuts.utils.LauncherShortcutManager
import ch.rmy.android.http_shortcuts.utils.Validation.isAcceptableHttpUrl
import ch.rmy.android.http_shortcuts.utils.Validation.isAcceptableUrl
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
import ch.rmy.android.http_shortcuts.variables.Variables
import ch.rmy.android.http_shortcuts.widget.WidgetManager
import ch.rmy.curlcommand.CurlCommand

class ShortcutEditorViewModel(application: Application) : BaseViewModel<ShortcutEditorViewModel.InitData, ShortcutEditorViewState>(application) {

    private val shortcutRepository = ShortcutRepository()
    private val temporaryShortcutRepository = TemporaryShortcutRepository()
    private val variableRepository = VariableRepository()
    private val widgetManager = WidgetManager()

    private val variablePlaceholderProvider = VariablePlaceholderProvider()

    private val variablePlaceholderColor by lazy {
        color(context, R.color.variable)
    }

    private var isSaving = false

    private var oldShortcut: Shortcut? = null

    private lateinit var shortcut: Shortcut

    private val categoryId
        get() = initData.categoryId

    private val shortcutId
        get() = initData.shortcutId

    private val executionType
        get() = initData.executionType

    override fun onInitializationStarted(data: InitData) {
        if (data.shortcutId == null) {
            temporaryShortcutRepository.createNewTemporaryShortcut(
                initialIcon = Icons.getRandomInitialIcon(context),
                executionType = executionType,
            )
        } else {
            shortcutRepository.createTemporaryShortcutFromShortcut(data.shortcutId)
        }
            .mapIfNotNull(data.curlCommand) {
                andThen(temporaryShortcutRepository.importFromCurl(it))
            }
            .subscribe(
                {
                    observeTemporaryShortcut()
                },
                {
                    finish()
                    showSnackbar(R.string.error_generic)
                },
            )
            .attachTo(destroyer)

        variableRepository.getObservableVariables()
            .subscribe { variables ->
                variablePlaceholderProvider.applyVariables(variables)
            }
            .attachTo(destroyer)
    }

    override fun initViewState() = ShortcutEditorViewState(
        toolbarTitle = StringResLocalizable(if (shortcutId != null) R.string.edit_shortcut else R.string.create_shortcut),
        shortcutExecutionType = executionType,
    )

    private fun observeTemporaryShortcut() {
        temporaryShortcutRepository.getObservableTemporaryShortcut()
            .subscribe { shortcut ->
                this.shortcut = shortcut
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
                        testButtonVisible = canExecute(),
                        saveButtonVisible = hasChanges(),
                        requestBodyButtonEnabled = shortcut.allowsBody(),
                        basicSettingsSubtitle = getBasicSettingsSubtitle(),
                        headersSubtitle = getHeadersSubtitle(),
                        requestBodySubtitle = getRequestBodySubtitle(),
                        requestBodySettingsSubtitle = getRequestBodySubtitle(),
                        authenticationSettingsSubtitle = getAuthenticationSubtitle(),
                        scriptingSubtitle = getScriptingSubtitle(),
                        triggerShortcutsSubtitle = getTriggerShortcutsSubtitle(),
                    )
                }
            }
            .attachTo(destroyer)
    }

    private fun hasChanges() =
        oldShortcut?.isSameAs(shortcut) == false || initData.curlCommand != null

    private fun canExecute() =
        !shortcut.type.usesUrl ||
            (shortcut.type.requiresHttpUrl && isAcceptableHttpUrl(shortcut.url)) ||
            (!shortcut.type.requiresHttpUrl && isAcceptableUrl(shortcut.url))

    private fun getToolbarSubtitle() =
        when (shortcut.type) {
            ShortcutExecutionType.BROWSER -> StringResLocalizable(R.string.subtitle_editor_toolbar_browser_shortcut)
            ShortcutExecutionType.SCRIPTING -> StringResLocalizable(R.string.subtitle_editor_toolbar_scripting_shortcut)
            ShortcutExecutionType.TRIGGER -> StringResLocalizable(R.string.subtitle_editor_toolbar_trigger_shortcut)
            else -> null
        }

    private fun getBasicSettingsSubtitle(): Localizable =
        enhancedWithVariables(
            if (shortcut.type == ShortcutExecutionType.BROWSER) {
                if (shortcut.url.let { it.isEmpty() || it == "http://" || it == "https://" }) {
                    StringResLocalizable(R.string.subtitle_basic_request_settings_url_only_prompt)
                } else {
                    shortcut.url.toLocalizable()
                }
            } else {
                if (shortcut.url.let { it.isEmpty() || it == "http://" || it == "https://" }) {
                    StringResLocalizable(R.string.subtitle_basic_request_settings_prompt)
                } else {
                    StringResLocalizable(
                        R.string.subtitle_basic_request_settings_pattern,
                        shortcut.method,
                        shortcut.url,
                    )
                }
            }
        )

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
                RequestBodyType.FILE -> StringResLocalizable(R.string.subtitle_request_body_file)
                else -> if (shortcut.bodyContent.isBlank()) {
                    StringResLocalizable(R.string.subtitle_request_body_none)
                } else {
                    StringResLocalizable(R.string.subtitle_request_body_custom, shortcut.contentType.ifEmpty { Shortcut.DEFAULT_CONTENT_TYPE })
                }
            }
        } else {
            StringResLocalizable(R.string.subtitle_request_body_not_available, shortcut.method)
        }

    private fun getAuthenticationSubtitle(): Localizable =
        StringResLocalizable(
            when (shortcut.authentication) {
                Shortcut.AUTHENTICATION_BASIC -> R.string.subtitle_authentication_basic
                Shortcut.AUTHENTICATION_DIGEST -> R.string.subtitle_authentication_digest
                Shortcut.AUTHENTICATION_BEARER -> R.string.subtitle_authentication_bearer
                else -> R.string.subtitle_authentication_none
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

    private fun enhancedWithVariables(localizable: Localizable): Localizable =
        Localizable.create { context ->
            Variables.rawPlaceholdersToVariableSpans(
                localizable.localize(context),
                variablePlaceholderProvider,
                variablePlaceholderColor,
            )
        }

    fun onShortcutIconChanged(icon: ShortcutIcon) {
        updateViewState {
            copy(shortcutIcon = icon)
        }
        performOperation(
            temporaryShortcutRepository.setIcon(icon)
        )
    }

    fun onShortcutNameChanged(name: String) {
        updateViewState {
            copy(shortcutName = name)
        }
        performOperation(
            temporaryShortcutRepository.setName(name)
        )
    }

    fun onShortcutDescriptionChanged(description: String) {
        updateViewState {
            copy(shortcutDescription = description)
        }
        performOperation(
            temporaryShortcutRepository.setDescription(description)
        )
    }

    fun onTestButtonClicked() {
        if (!currentViewState.testButtonVisible) {
            return
        }
        waitForOperationsToFinish {
            openActivity(ExecuteActivity.IntentBuilder(TEMPORARY_ID))
        }
    }

    fun onSaveButtonClicked() {
        if (!currentViewState.saveButtonVisible || isSaving) {
            return
        }
        isSaving = true
        waitForOperationsToFinish {
            trySave()
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
        val isNewShortcut = shortcutId == null
        val shortcutId = shortcutId ?: newUUID()
        performOperation(
            shortcutRepository.copyTemporaryShortcutToShortcut(shortcutId, categoryId?.takeIf { isNewShortcut })
                .andThen(temporaryShortcutRepository.deleteTemporaryShortcut()),
        ) {
            onSaveSuccessful(shortcutId)
        }
    }

    private fun onSaveSuccessful(shortcutId: String) {
        LauncherShortcutManager.updatePinnedShortcut(context, shortcutId, shortcut.name, shortcut.icon)
        performOperation(widgetManager.updateWidgets(context, shortcutId))
        waitForOperationsToFinish {
            CleanUpWorker.schedule(context)
            finish(result = RESULT_OK, intent = Intent().putExtra(RESULT_SHORTCUT_ID, shortcutId))
        }
    }

    fun onBackPressed() {
        waitForOperationsToFinish {
            if (hasChanges()) {
                showDiscardDialog()
            } else {
                onDiscardDialogConfirmed()
            }
        }
    }

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
        performOperation(temporaryShortcutRepository.deleteTemporaryShortcut())
        waitForOperationsToFinish {
            CleanUpWorker.schedule(context)
            finish(result = Activity.RESULT_CANCELED)
        }
    }

    fun onBasicRequestSettingsButtonClicked() {
        openActivity(BasicRequestSettingsActivity.IntentBuilder())
    }

    fun onHeadersButtonClicked() {
        openActivity(RequestHeadersActivity.IntentBuilder())
    }

    fun onRequestBodyButtonClicked() {
        openActivity(RequestBodyActivity.IntentBuilder())
    }

    fun onAuthenticationButtonClicked() {
        openActivity(AuthenticationActivity.IntentBuilder())
    }

    fun onResponseHandlingButtonClicked() {
        openActivity(ResponseActivity.IntentBuilder())
    }

    fun onScriptingButtonClicked() {
        openActivity(
            ScriptingActivity.IntentBuilder()
                .shortcutId(shortcutId)
        )
    }

    fun onTriggerShortcutsButtonClicked() {
        openActivity(
            TriggerShortcutsActivity.IntentBuilder()
                .shortcutId(shortcutId)
        )
    }

    fun onExecutionSettingsButtonClicked() {
        openActivity(ExecutionSettingsActivity.IntentBuilder())
    }

    fun onAdvancedSettingsButtonClicked() {
        openActivity(AdvancedSettingsActivity.IntentBuilder())
    }

    data class InitData(
        val categoryId: String?,
        val shortcutId: String?,
        val curlCommand: CurlCommand?,
        val executionType: ShortcutExecutionType,
    )
}

package ch.rmy.android.http_shortcuts.activities.editor

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.activities.ExecuteActivity
import ch.rmy.android.http_shortcuts.activities.editor.advancedsettings.AdvancedSettingsActivity
import ch.rmy.android.http_shortcuts.activities.editor.authentication.AuthenticationActivity
import ch.rmy.android.http_shortcuts.activities.editor.basicsettings.BasicRequestSettingsActivity
import ch.rmy.android.http_shortcuts.activities.editor.body.RequestBodyActivity
import ch.rmy.android.http_shortcuts.activities.editor.headers.RequestHeadersActivity
import ch.rmy.android.http_shortcuts.activities.editor.miscsettings.MiscSettingsActivity
import ch.rmy.android.http_shortcuts.activities.editor.response.ResponseActivity
import ch.rmy.android.http_shortcuts.activities.editor.scripting.ScriptingActivity
import ch.rmy.android.http_shortcuts.activities.editor.shortcuts.TriggerShortcutsActivity
import ch.rmy.android.http_shortcuts.data.enums.ShortcutExecutionType
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.dialogs.DialogBuilder
import ch.rmy.android.http_shortcuts.extensions.attachTo
import ch.rmy.android.http_shortcuts.extensions.bindViewModel
import ch.rmy.android.http_shortcuts.extensions.color
import ch.rmy.android.http_shortcuts.extensions.consume
import ch.rmy.android.http_shortcuts.extensions.focus
import ch.rmy.android.http_shortcuts.extensions.logException
import ch.rmy.android.http_shortcuts.extensions.logInfo
import ch.rmy.android.http_shortcuts.extensions.observeTextChanges
import ch.rmy.android.http_shortcuts.extensions.setMaxLength
import ch.rmy.android.http_shortcuts.extensions.setTextSafely
import ch.rmy.android.http_shortcuts.extensions.showSnackbar
import ch.rmy.android.http_shortcuts.extensions.showToast
import ch.rmy.android.http_shortcuts.extensions.startActivity
import ch.rmy.android.http_shortcuts.extensions.type
import ch.rmy.android.http_shortcuts.extensions.visible
import ch.rmy.android.http_shortcuts.icons.IconPicker
import ch.rmy.android.http_shortcuts.icons.IconView
import ch.rmy.android.http_shortcuts.utils.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.utils.LauncherShortcutManager
import ch.rmy.android.http_shortcuts.utils.Validation
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
import ch.rmy.android.http_shortcuts.variables.Variables
import ch.rmy.android.http_shortcuts.views.PanelButton
import ch.rmy.android.http_shortcuts.widget.WidgetManager
import ch.rmy.curlcommand.CurlCommand
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
import kotterknife.bindView
import java.util.concurrent.TimeUnit

class ShortcutEditorActivity : BaseActivity() {

    private val shortcutId by lazy {
        intent.getStringExtra(EXTRA_SHORTCUT_ID)
    }
    private val categoryId by lazy {
        intent.getStringExtra(EXTRA_CATEGORY_ID)
    }
    private val curlCommand by lazy {
        intent.getSerializableExtra(EXTRA_CURL_COMMAND) as CurlCommand?
    }
    private val executionType: String by lazy {
        intent.getStringExtra(EXTRA_EXECUTION_TYPE) ?: ShortcutExecutionType.APP.type
    }

    private val viewModel: ShortcutEditorViewModel by bindViewModel()
    private val shortcutData by lazy {
        viewModel.shortcut
    }
    private val variablesData by lazy {
        viewModel.variables
    }
    private val variablePlaceholderProvider by lazy {
        VariablePlaceholderProvider(variablesData)
    }

    // Views
    private val iconView: IconView by bindView(R.id.input_icon)
    private val iconContainer: View by bindView(R.id.icon_container)
    private val nameView: EditText by bindView(R.id.input_shortcut_name)
    private val descriptionView: EditText by bindView(R.id.input_description)
    private val basicRequestSettingsButton: PanelButton by bindView(R.id.button_basic_request_settings)
    private val headersButton: PanelButton by bindView(R.id.button_headers)
    private val requestBodyButton: PanelButton by bindView(R.id.button_request_body)
    private val authenticationButton: PanelButton by bindView(R.id.button_authentication)
    private val responseHandlingButton: PanelButton by bindView(R.id.button_response_handling)
    private val scriptingButton: PanelButton by bindView(R.id.button_scripting)
    private val triggerShortcutsButton: PanelButton by bindView(R.id.button_trigger_shortcuts)
    private val miscSettingsButton: PanelButton by bindView(R.id.button_misc_settings)
    private val advancedTechnicalSettingsButton: PanelButton by bindView(R.id.button_advanced_technical_settings)
    private val dividerBelowBasicSettings: View by bindView(R.id.divider_below_basic_request_settings)
    private val dividerBelowHeaders: View by bindView(R.id.divider_below_headers)
    private val dividerBelowRequestBody: View by bindView(R.id.divider_below_request_body)
    private val dividerBelowAuthentication: View by bindView(R.id.divider_below_authentication)
    private val dividerBelowScripting: View by bindView(R.id.divider_below_scripting)

    private val variablePlaceholderColor by lazy {
        color(context, R.color.variable)
    }

    private val iconPicker by lazy {
        IconPicker(this) { iconName ->
            viewModel.setIconName(iconName)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle(if (shortcutId != null) {
            R.string.edit_shortcut
        } else {
            R.string.create_shortcut
        })
        setContentView(R.layout.activity_loading)
        initViewModel()
    }

    private fun initViewModel() {
        viewModel.init(categoryId, shortcutId, curlCommand, executionType)
            .subscribe({
                initViews()
            }, { e ->
                handleUnknownError(e)
                finish()
            })
            .attachTo(destroyer)
    }

    private fun initViews() {
        setContentView(R.layout.activity_shortcut_editor_overview)
        nameView.setMaxLength(Shortcut.NAME_MAX_LENGTH)
        invalidateOptionsMenu()
        bindViewsToViewModel()
        bindClickListeners()
        bindTextChangeListeners()
    }

    private fun bindViewsToViewModel() {
        shortcutData.observe(this) {
            updateShortcutViews()
            invalidateOptionsMenu()
        }
        variablesData.observe(this) {
            updateShortcutViews()
        }
    }

    private fun updateShortcutViews() {
        val shortcut = shortcutData.value ?: return
        iconView.setIcon(shortcut.iconName, animated = true)
        nameView.setTextSafely(shortcut.name)
        descriptionView.setTextSafely(shortcut.description)

        toolbar?.subtitle = viewModel.getToolbarSubtitle(shortcut)

        val type = shortcut.type
        basicRequestSettingsButton.visible = type.usesUrl
        dividerBelowBasicSettings.visible = type.usesUrl
        headersButton.visible = type.usesRequestOptions
        dividerBelowHeaders.visible = type.usesRequestOptions
        requestBodyButton.visible = type.usesRequestOptions
        dividerBelowRequestBody.visible = type.usesRequestOptions
        authenticationButton.visible = type.usesRequestOptions
        dividerBelowAuthentication.visible = type.usesRequestOptions
        responseHandlingButton.visible = type.usesResponse
        advancedTechnicalSettingsButton.visible = type.usesRequestOptions
        scriptingButton.visible = type.usesScriptingEditor
        triggerShortcutsButton.visible = type == ShortcutExecutionType.TRIGGER
        dividerBelowScripting.visible = type.usesScriptingEditor || type == ShortcutExecutionType.TRIGGER

        basicRequestSettingsButton.subtitle = viewModel.getBasicSettingsSubtitle(shortcut)
            .let { subtitle ->
                Variables.rawPlaceholdersToVariableSpans(
                    subtitle,
                    variablePlaceholderProvider,
                    variablePlaceholderColor
                )
            }

        if (type.usesRequestOptions) {
            headersButton.subtitle = viewModel.getHeadersSettingsSubtitle(shortcut)
            requestBodyButton.subtitle = viewModel.getRequestBodySettingsSubtitle(shortcut)
            authenticationButton.subtitle = viewModel.getAuthenticationSettingsSubtitle(shortcut)

            requestBodyButton.isEnabled = shortcut.allowsBody()
        }
        scriptingButton.subtitle = viewModel.getScriptingSubtitle(shortcut)

        if (type == ShortcutExecutionType.TRIGGER) {
            triggerShortcutsButton.subtitle = viewModel.getTriggerShortcutsSubtitle(shortcut)
        }
    }

    private fun bindClickListeners() {
        iconContainer.setOnClickListener {
            iconPicker.openIconSelectionDialog()
        }
        basicRequestSettingsButton.setOnClickListener {
            BasicRequestSettingsActivity.IntentBuilder(context)
                .build()
                .startActivity(this)
        }
        headersButton.setOnClickListener {
            RequestHeadersActivity.IntentBuilder(context)
                .build()
                .startActivity(this)
        }
        requestBodyButton.setOnClickListener {
            RequestBodyActivity.IntentBuilder(context)
                .build()
                .startActivity(this)
        }
        authenticationButton.setOnClickListener {
            AuthenticationActivity.IntentBuilder(context)
                .build()
                .startActivity(this)
        }
        responseHandlingButton.setOnClickListener {
            ResponseActivity.IntentBuilder(context)
                .build()
                .startActivity(this)
        }
        scriptingButton.setOnClickListener {
            ScriptingActivity.IntentBuilder(context)
                .shortcutId(shortcutId)
                .build()
                .startActivity(this)
        }
        triggerShortcutsButton.setOnClickListener {
            TriggerShortcutsActivity.IntentBuilder(context)
                .shortcutId(shortcutId)
                .build()
                .startActivity(this)
        }
        miscSettingsButton.setOnClickListener {
            MiscSettingsActivity.IntentBuilder(context)
                .build()
                .startActivity(this)
        }
        advancedTechnicalSettingsButton.setOnClickListener {
            AdvancedSettingsActivity.IntentBuilder(context)
                .build()
                .startActivity(this)
        }
    }

    private fun bindTextChangeListeners() {
        bindTextChangeListener(nameView) { shortcutData.value?.name }
        bindTextChangeListener(descriptionView) { shortcutData.value?.description }
    }

    private fun bindTextChangeListener(textView: EditText, currentValueProvider: () -> String?) {
        textView.observeTextChanges()
            .debounce(300, TimeUnit.MILLISECONDS)
            .observeOn(mainThread())
            .filter { it.toString() != currentValueProvider.invoke() }
            .concatMapCompletable { updateViewModelFromViews() }
            .subscribe()
            .attachTo(destroyer)
    }

    override val navigateUpIcon = R.drawable.ic_clear

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (viewModel.isInitialized) {
            menuInflater.inflate(R.menu.editor_activity_menu, menu)
            menu.findItem(R.id.action_test_shortcut).isVisible = canExecute()
        }
        return super.onCreateOptionsMenu(menu)
    }

    private fun canExecute(): Boolean =
        shortcutData.value
            ?.let { !it.type.usesUrl || Validation.isAcceptableUrl(it.url) }
            ?: false

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home -> consume { onCloseEditor() }
        R.id.action_save_shortcut -> consume {
            logInfo("Clicked Save button in shortcut editor")
            trySaveShortcut()
        }
        R.id.action_test_shortcut -> consume {
            logInfo("Clicked Test button in shortcut editor")
            testShortcut()
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun onCloseEditor() {
        if (viewModel.isInitialized) {
            updateViewModelFromViews()
                .subscribe {
                    if (viewModel.hasChanges()) {
                        DialogBuilder(context)
                            .message(R.string.confirm_discard_changes_message)
                            .positive(R.string.dialog_discard) { cancelAndClose() }
                            .negative(R.string.dialog_cancel)
                            .showIfPossible()
                    } else {
                        cancelAndClose()
                    }
                }
                .attachTo(destroyer)
        } else {
            cancelAndClose()
        }
    }

    private fun cancelAndClose() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    private fun trySaveShortcut() {
        if (viewModel.isSaving) {
            logInfo("Saving already in progress")
            return
        }
        viewModel.isSaving = true
        updateViewModelFromViews()
            .andThen(viewModel.trySave())
            .observeOn(mainThread())
            .subscribe({ saveResult ->
                logInfo("Saving shortcut successful")
                if (saveResult.name != null) {
                    LauncherShortcutManager.updatePinnedShortcut(context, saveResult.id, saveResult.name, saveResult.iconName)
                }
                WidgetManager.updateWidgets(context, saveResult.id)
                setResult(RESULT_OK, Intent().putExtra(RESULT_SHORTCUT_ID, saveResult.id))
                finish()
            }, { e ->
                viewModel.isSaving = false
                logInfo("Saving shortcut failed: ${e.message}")
                if (e is ShortcutValidationError) {
                    when (e.type) {
                        ShortcutEditorViewModel.VALIDATION_ERROR_EMPTY_NAME -> {
                            showSnackbar(R.string.validation_name_not_empty, long = true)
                            nameView.focus()
                        }
                        ShortcutEditorViewModel.VALIDATION_ERROR_INVALID_URL -> {
                            showSnackbar(R.string.validation_url_invalid, long = true)
                        }
                        else -> handleUnknownError(e)
                    }
                } else {
                    handleUnknownError(e)
                }
            })
            .attachTo(destroyer)
    }

    private fun testShortcut() {
        updateViewModelFromViews()
            .subscribe {
                ExecuteActivity.IntentBuilder(context, Shortcut.TEMPORARY_ID)
                    .build()
                    .startActivity(this)
            }
            .attachTo(destroyer)
    }

    private fun updateViewModelFromViews(): Completable =
        viewModel.setNameAndDescription(
            name = nameView.text.toString(),
            description = descriptionView.text.toString()
        )

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        iconPicker.handleResult(requestCode, resultCode, intent)
    }

    override fun onBackPressed() {
        onCloseEditor()
    }

    private fun handleUnknownError(e: Throwable) {
        logException(e)
        showToast(R.string.error_generic)
    }

    class IntentBuilder(context: Context) : BaseIntentBuilder(context, ShortcutEditorActivity::class.java) {

        fun shortcutId(shortcutId: String) = also {
            intent.putExtra(EXTRA_SHORTCUT_ID, shortcutId)
        }

        fun categoryId(categoryId: String) = also {
            intent.putExtra(EXTRA_CATEGORY_ID, categoryId)
        }

        fun curlCommand(command: CurlCommand) = also {
            intent.putExtra(EXTRA_CURL_COMMAND, command)
        }

        fun executionType(type: ShortcutExecutionType) = also {
            intent.putExtra(EXTRA_EXECUTION_TYPE, type.type)
        }

    }

    companion object {

        private const val EXTRA_SHORTCUT_ID = "shortcutId"
        private const val EXTRA_CATEGORY_ID = "categoryId"
        private const val EXTRA_CURL_COMMAND = "curlCommand"
        private const val EXTRA_EXECUTION_TYPE = "executionType"

        const val RESULT_SHORTCUT_ID = "shortcutId"

    }

}
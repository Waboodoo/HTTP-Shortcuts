package ch.rmy.android.http_shortcuts.activities.editor

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
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
import ch.rmy.android.http_shortcuts.data.enums.ShortcutExecutionType
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.databinding.ActivityShortcutEditorOverviewBinding
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
import ch.rmy.android.http_shortcuts.extensions.type
import ch.rmy.android.http_shortcuts.extensions.visible
import ch.rmy.android.http_shortcuts.icons.IconPicker
import ch.rmy.android.http_shortcuts.utils.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.utils.LauncherShortcutManager
import ch.rmy.android.http_shortcuts.utils.Validation
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
import ch.rmy.android.http_shortcuts.variables.Variables
import ch.rmy.android.http_shortcuts.widget.WidgetManager
import ch.rmy.curlcommand.CurlCommand
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers.mainThread
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

    private lateinit var binding: ActivityShortcutEditorOverviewBinding

    private val variablePlaceholderColor by lazy {
        color(context, R.color.variable)
    }

    private val iconPicker by lazy {
        IconPicker(this) { icon ->
            viewModel.setIcon(icon)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTitle(
            if (shortcutId != null) {
                R.string.edit_shortcut
            } else {
                R.string.create_shortcut
            }
        )
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
        binding = applyBinding(ActivityShortcutEditorOverviewBinding.inflate(layoutInflater))
        binding.inputShortcutName.setMaxLength(Shortcut.NAME_MAX_LENGTH)
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
        binding.inputIcon.setIcon(shortcut.icon, animated = true)
        binding.inputShortcutName.setTextSafely(shortcut.name)
        binding.inputDescription.setTextSafely(shortcut.description)

        toolbar?.subtitle = viewModel.getToolbarSubtitle(shortcut)

        val type = shortcut.type
        binding.buttonBasicRequestSettings.visible = type.usesUrl
        binding.dividerBelowBasicRequestSettings.visible = type.usesUrl
        binding.buttonHeaders.visible = type.usesRequestOptions
        binding.dividerBelowHeaders.visible = type.usesRequestOptions
        binding.buttonRequestBody.visible = type.usesRequestOptions
        binding.dividerBelowRequestBody.visible = type.usesRequestOptions
        binding.buttonAuthentication.visible = type.usesRequestOptions
        binding.dividerBelowAuthentication.visible = type.usesRequestOptions
        binding.buttonResponseHandling.visible = type.usesResponse
        binding.buttonAdvancedTechnicalSettings.visible = type.usesRequestOptions
        binding.buttonScripting.visible = type.usesScriptingEditor
        binding.buttonTriggerShortcuts.visible = type == ShortcutExecutionType.TRIGGER
        binding.dividerBelowScripting.visible = type.usesScriptingEditor || type == ShortcutExecutionType.TRIGGER

        binding.buttonBasicRequestSettings.subtitle = viewModel.getBasicSettingsSubtitle(shortcut)
            .let { subtitle ->
                Variables.rawPlaceholdersToVariableSpans(
                    subtitle,
                    variablePlaceholderProvider,
                    variablePlaceholderColor
                )
            }

        if (type.usesRequestOptions) {
            binding.buttonHeaders.subtitle = viewModel.getHeadersSettingsSubtitle(shortcut)
            binding.buttonRequestBody.subtitle = viewModel.getRequestBodySettingsSubtitle(shortcut)
            binding.buttonAuthentication.subtitle = viewModel.getAuthenticationSettingsSubtitle(shortcut)

            binding.buttonRequestBody.isEnabled = shortcut.allowsBody()
        }
        binding.buttonScripting.subtitle = viewModel.getScriptingSubtitle(shortcut)

        if (type == ShortcutExecutionType.TRIGGER) {
            binding.buttonTriggerShortcuts.subtitle = viewModel.getTriggerShortcutsSubtitle(shortcut)
        }
    }

    private fun bindClickListeners() {
        binding.iconContainer.setOnClickListener {
            iconPicker.openIconSelectionDialog()
        }
        binding.buttonBasicRequestSettings.setOnClickListener {
            BasicRequestSettingsActivity.IntentBuilder(context)
                .startActivity(this)
        }
        binding.buttonHeaders.setOnClickListener {
            RequestHeadersActivity.IntentBuilder(context)
                .startActivity(this)
        }
        binding.buttonRequestBody.setOnClickListener {
            RequestBodyActivity.IntentBuilder(context)
                .startActivity(this)
        }
        binding.buttonAuthentication.setOnClickListener {
            AuthenticationActivity.IntentBuilder(context)
                .startActivity(this)
        }
        binding.buttonResponseHandling.setOnClickListener {
            ResponseActivity.IntentBuilder(context)
                .startActivity(this)
        }
        binding.buttonScripting.setOnClickListener {
            ScriptingActivity.IntentBuilder(context)
                .shortcutId(shortcutId)
                .startActivity(this)
        }
        binding.buttonTriggerShortcuts.setOnClickListener {
            TriggerShortcutsActivity.IntentBuilder(context)
                .shortcutId(shortcutId)
                .startActivity(this)
        }
        binding.buttonExecutionSettings.setOnClickListener {
            ExecutionSettingsActivity.IntentBuilder(context)
                .startActivity(this)
        }
        binding.buttonAdvancedTechnicalSettings.setOnClickListener {
            AdvancedSettingsActivity.IntentBuilder(context)
                .startActivity(this)
        }
    }

    private fun bindTextChangeListeners() {
        bindTextChangeListener(binding.inputShortcutName) { shortcutData.value?.name }
        bindTextChangeListener(binding.inputDescription) { shortcutData.value?.description }
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
            ?.let {
                !it.type.usesUrl ||
                    (it.type.requiresHttpUrl && Validation.isAcceptableHttpUrl(it.url)) ||
                    (!it.type.requiresHttpUrl && Validation.isAcceptableUrl(it.url))
            }
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
        updateViewModelFromViews()
            .andThen(viewModel.trySave())
            .observeOn(mainThread())
            .subscribe({ saveResult ->
                try {
                    logInfo("Saving shortcut successful")
                    if (saveResult.name != null && saveResult.icon != null) {
                        LauncherShortcutManager.updatePinnedShortcut(context, saveResult.id, saveResult.name, saveResult.icon)
                    }
                    WidgetManager.updateWidgets(context, saveResult.id)
                    setResult(RESULT_OK, Intent().putExtra(RESULT_SHORTCUT_ID, saveResult.id))
                    finish()
                } catch (e: Exception) {
                    handleUnknownError(e)
                }
            }, { e ->
                logInfo("Saving shortcut failed: ${e.message}")
                if (e is ShortcutValidationError) {
                    when (e.type) {
                        ShortcutEditorViewModel.VALIDATION_ERROR_EMPTY_NAME -> {
                            showSnackbar(R.string.validation_name_not_empty, long = true)
                            binding.inputShortcutName.focus()
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
                    .startActivity(this)
            }
            .attachTo(destroyer)
    }

    private fun updateViewModelFromViews(): Completable =
        viewModel.setNameAndDescription(
            name = binding.inputShortcutName.text.toString(),
            description = binding.inputDescription.text.toString()
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

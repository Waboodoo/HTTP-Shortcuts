package ch.rmy.android.http_shortcuts.activities.editor

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.launch
import androidx.core.view.isVisible
import ch.rmy.android.framework.extensions.attachTo
import ch.rmy.android.framework.extensions.bindViewModel
import ch.rmy.android.framework.extensions.consume
import ch.rmy.android.framework.extensions.createIntent
import ch.rmy.android.framework.extensions.focus
import ch.rmy.android.framework.extensions.isVisible
import ch.rmy.android.framework.extensions.launch
import ch.rmy.android.framework.extensions.logInfo
import ch.rmy.android.framework.extensions.observe
import ch.rmy.android.framework.extensions.observeTextChanges
import ch.rmy.android.framework.extensions.setMaxLength
import ch.rmy.android.framework.extensions.setSubtitle
import ch.rmy.android.framework.extensions.setTextSafely
import ch.rmy.android.framework.ui.BaseActivityResultContract
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.framework.viewmodel.ViewModelEvent
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.activities.icons.IconPickerActivity
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.enums.ShortcutExecutionType
import ch.rmy.android.http_shortcuts.data.models.ShortcutModel
import ch.rmy.android.http_shortcuts.databinding.ActivityShortcutEditorOverviewBinding
import ch.rmy.android.http_shortcuts.icons.IpackPickerContract
import ch.rmy.curlcommand.CurlCommand

class ShortcutEditorActivity : BaseActivity() {

    private val pickCustomIcon = registerForActivityResult(IconPickerActivity.PickIcon) { icon ->
        icon?.let(viewModel::onShortcutIconChanged)
    }
    private val pickIpackIcon = registerForActivityResult(IpackPickerContract) { icon ->
        icon?.let(viewModel::onShortcutIconChanged)
    }

    private val viewModel: ShortcutEditorViewModel by bindViewModel()

    private lateinit var binding: ActivityShortcutEditorOverviewBinding
    private var saveMenuItem: MenuItem? = null
    private var testMenuItem: MenuItem? = null

    override fun onCreated(savedState: Bundle?) {
        viewModel.initialize(
            ShortcutEditorViewModel.InitData(
                categoryId = intent.getStringExtra(EXTRA_CATEGORY_ID),
                shortcutId = intent.getStringExtra(EXTRA_SHORTCUT_ID),
                curlCommand = intent.getSerializableExtra(EXTRA_CURL_COMMAND) as CurlCommand?,
                executionType = intent.getStringExtra(EXTRA_EXECUTION_TYPE)
                    ?.let(ShortcutExecutionType::get)
                    ?: ShortcutExecutionType.APP,
                recoveryMode = intent.getBooleanExtra(EXTRA_RECOVERY_MODE, false),
            ),
        )
        initViews()
        initUserInputBindings()
        initViewModelBindings()
    }

    private fun initViews() {
        binding = applyBinding(ActivityShortcutEditorOverviewBinding.inflate(layoutInflater))
        binding.inputShortcutName.setMaxLength(ShortcutModel.NAME_MAX_LENGTH)
        binding.mainView.isVisible = false
        title = ""
    }

    private fun initUserInputBindings() {
        binding.iconContainer.setOnClickListener {
            viewModel.onIconClicked()
        }
        binding.buttonBasicRequestSettings.setOnClickListener {
            viewModel.onBasicRequestSettingsButtonClicked()
        }
        binding.buttonHeaders.setOnClickListener {
            viewModel.onHeadersButtonClicked()
        }
        binding.buttonRequestBody.setOnClickListener {
            viewModel.onRequestBodyButtonClicked()
        }
        binding.buttonAuthentication.setOnClickListener {
            viewModel.onAuthenticationButtonClicked()
        }
        binding.buttonResponseHandling.setOnClickListener {
            viewModel.onResponseHandlingButtonClicked()
        }
        binding.buttonScripting.setOnClickListener {
            viewModel.onScriptingButtonClicked()
        }
        binding.buttonTriggerShortcuts.setOnClickListener {
            viewModel.onTriggerShortcutsButtonClicked()
        }
        binding.buttonExecutionSettings.setOnClickListener {
            viewModel.onExecutionSettingsButtonClicked()
        }
        binding.buttonAdvancedTechnicalSettings.setOnClickListener {
            viewModel.onAdvancedSettingsButtonClicked()
        }

        binding.inputShortcutName
            .observeTextChanges()
            .subscribe { name ->
                viewModel.onShortcutNameChanged(name.toString())
            }
            .attachTo(destroyer)
        binding.inputDescription
            .observeTextChanges()
            .subscribe { description ->
                viewModel.onShortcutDescriptionChanged(description.toString())
            }
            .attachTo(destroyer)
    }

    private fun initViewModelBindings() {
        viewModel.viewState.observe(this) { viewState ->
            binding.loadingIndicator.isVisible = false
            binding.mainView.isVisible = true
            val type = viewState.shortcutExecutionType
            setTitle(viewState.toolbarTitle)
            setSubtitle(viewState.toolbarSubtitle)
            binding.inputIcon.setIcon(viewState.shortcutIcon, animated = true)
            binding.inputIcon.alpha = if (viewState.iconLoading) 0.7f else 1f
            binding.iconContainer.isClickable = viewState.isIconClickable
            binding.iconLoadingIndicator.isVisible = viewState.iconLoading
            binding.inputShortcutName.setTextSafely(viewState.shortcutName)
            binding.inputShortcutName.isEnabled = !viewState.isInputDisabled
            binding.inputDescription.setTextSafely(viewState.shortcutDescription)
            binding.inputDescription.isEnabled = !viewState.isInputDisabled
            binding.buttonBasicRequestSettings.isVisible = type.usesUrl
            binding.buttonBasicRequestSettings.isEnabled = !viewState.isInputDisabled
            binding.dividerBelowBasicRequestSettings.isVisible = type.usesUrl
            binding.buttonHeaders.isVisible = type.usesRequestOptions
            binding.buttonHeaders.isEnabled = !viewState.isInputDisabled
            binding.dividerBelowHeaders.isVisible = type.usesRequestOptions
            binding.buttonRequestBody.isVisible = type.usesRequestOptions
            binding.buttonRequestBody.isEnabled = !viewState.isInputDisabled
            binding.dividerBelowRequestBody.isVisible = type.usesRequestOptions
            binding.buttonAuthentication.isVisible = type.usesRequestOptions
            binding.buttonAuthentication.isEnabled = !viewState.isInputDisabled
            binding.dividerBelowAuthentication.isVisible = type.usesRequestOptions
            binding.buttonResponseHandling.isVisible = type.usesResponse
            binding.buttonResponseHandling.isEnabled = !viewState.isInputDisabled
            binding.buttonAdvancedTechnicalSettings.isVisible = type.usesRequestOptions
            binding.buttonAdvancedTechnicalSettings.isEnabled = !viewState.isInputDisabled
            binding.buttonScripting.isVisible = type.usesScriptingEditor
            binding.buttonScripting.isEnabled = !viewState.isInputDisabled
            binding.buttonTriggerShortcuts.isVisible = type == ShortcutExecutionType.TRIGGER
            binding.buttonTriggerShortcuts.isEnabled = !viewState.isInputDisabled
            binding.buttonExecutionSettings.isEnabled = !viewState.isInputDisabled
            binding.dividerBelowScripting.isVisible = type.usesScriptingEditor || type == ShortcutExecutionType.TRIGGER
            binding.buttonBasicRequestSettings.setSubtitle(viewState.basicSettingsSubtitle)
            binding.buttonHeaders.setSubtitle(viewState.headersSubtitle)
            binding.buttonRequestBody.setSubtitle(viewState.requestBodySettingsSubtitle)
            binding.buttonAuthentication.setSubtitle(viewState.authenticationSettingsSubtitle)
            binding.buttonScripting.setSubtitle(viewState.scriptingSubtitle)
            binding.buttonTriggerShortcuts.setSubtitle(viewState.triggerShortcutsSubtitle)
            binding.buttonRequestBody.isEnabled = viewState.requestBodyButtonEnabled
            testMenuItem?.isVisible = viewState.isExecuteButtonVisible
            saveMenuItem?.isVisible = viewState.isSaveButtonVisible
            setDialogState(viewState.dialogState, viewModel)
        }
        viewModel.events.observe(this, ::handleEvent)
    }

    override val navigateUpIcon = R.drawable.ic_clear

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.editor_activity_menu, menu)
        saveMenuItem = menu.findItem(R.id.action_save_shortcut)
        testMenuItem = menu.findItem(R.id.action_test_shortcut)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId) {
            R.id.action_save_shortcut -> consume {
                logInfo("Clicked Save button in shortcut editor")
                viewModel.onSaveButtonClicked()
            }
            R.id.action_test_shortcut -> consume {
                logInfo("Clicked Test button in shortcut editor")
                viewModel.onTestButtonClicked()
            }
            else -> super.onOptionsItemSelected(item)
        }

    override fun handleEvent(event: ViewModelEvent) {
        when (event) {
            is ShortcutEditorEvent.FocusNameInputField -> {
                binding.inputShortcutName.focus()
            }
            is ShortcutEditorEvent.OpenCustomIconPicker -> {
                pickCustomIcon.launch()
            }
            is ShortcutEditorEvent.OpenIpackIconPicker -> {
                pickIpackIcon.launch()
            }
            else -> super.handleEvent(event)
        }
    }

    override fun onBackPressed() {
        viewModel.onBackPressed()
    }

    object OpenShortcutEditor : BaseActivityResultContract<IntentBuilder, String?>(::IntentBuilder) {

        private const val RESULT_SHORTCUT_ID = "shortcutId"

        override fun parseResult(resultCode: Int, intent: Intent?): ShortcutId? =
            intent?.getStringExtra(RESULT_SHORTCUT_ID)

        fun createResult(shortcutId: ShortcutId) =
            createIntent {
                putExtra(RESULT_SHORTCUT_ID, shortcutId)
            }
    }

    class IntentBuilder : BaseIntentBuilder(ShortcutEditorActivity::class) {

        fun shortcutId(shortcutId: ShortcutId) = also {
            intent.putExtra(EXTRA_SHORTCUT_ID, shortcutId)
        }

        fun categoryId(categoryId: CategoryId) = also {
            intent.putExtra(EXTRA_CATEGORY_ID, categoryId)
        }

        fun curlCommand(command: CurlCommand) = also {
            intent.putExtra(EXTRA_CURL_COMMAND, command)
        }

        fun executionType(type: ShortcutExecutionType) = also {
            intent.putExtra(EXTRA_EXECUTION_TYPE, type.type)
        }

        fun recoveryMode() = also {
            intent.putExtra(EXTRA_RECOVERY_MODE, true)
        }
    }

    companion object {

        private const val EXTRA_SHORTCUT_ID = "shortcutId"
        private const val EXTRA_CATEGORY_ID = "categoryId"
        private const val EXTRA_CURL_COMMAND = "curlCommand"
        private const val EXTRA_EXECUTION_TYPE = "executionType"
        private const val EXTRA_RECOVERY_MODE = "recoveryMode"
    }
}

package ch.rmy.android.http_shortcuts.activities.editor.scripting.codesnippets

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.launch
import androidx.recyclerview.widget.LinearLayoutManager
import ch.rmy.android.framework.extensions.bindViewModel
import ch.rmy.android.framework.extensions.consume
import ch.rmy.android.framework.extensions.createIntent
import ch.rmy.android.framework.extensions.launch
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.extensions.observe
import ch.rmy.android.framework.extensions.showToast
import ch.rmy.android.framework.ui.BaseActivityResultContract
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.framework.viewmodel.ViewModelEvent
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.activities.icons.IconPickerActivity
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.databinding.ActivityCodeSnippetPickerBinding
import ch.rmy.android.http_shortcuts.icons.IpackPickerContract
import ch.rmy.android.http_shortcuts.plugin.TaskerTaskPickerContract
import ch.rmy.android.http_shortcuts.utils.RingtonePickerContract

class CodeSnippetPickerActivity : BaseActivity() {

    private val pickCustomIcon = registerForActivityResult(IconPickerActivity.PickIcon) { icon ->
        icon?.let(viewModel::onIconSelected)
    }
    private val pickIpackIcon = registerForActivityResult(IpackPickerContract) { icon ->
        icon?.let(viewModel::onIconSelected)
    }
    private val pickRingtone = registerForActivityResult(RingtonePickerContract) { ringtone ->
        ringtone?.let(viewModel::onRingtoneSelected)
    }
    private val pickTaskerTask = registerForActivityResult(TaskerTaskPickerContract) { taskName ->
        taskName?.let(viewModel::onTaskerTaskSelected)
    }

    private val viewModel: CodeSnippetPickerViewModel by bindViewModel()

    private val adapter = CodeSnippetAdapter()

    private lateinit var binding: ActivityCodeSnippetPickerBinding

    override fun onCreated(savedState: Bundle?) {
        viewModel.initialize(
            CodeSnippetPickerViewModel.InitData(
                currentShortcutId = intent.getStringExtra(EXTRA_SHORTCUT_ID),
                includeResponseOptions = intent.getBooleanExtra(EXTRA_INCLUDE_RESPONSE_OPTIONS, false),
                includeNetworkErrorOption = intent.getBooleanExtra(EXTRA_INCLUDE_NETWORK_ERROR_OPTION, false),
                includeFileOptions = intent.getBooleanExtra(EXTRA_INCLUDE_FILE_OPTIONS, false),
            )
        )
        initViews()
        initUserInputBindings()
        initViewModelBindings()
    }

    private fun initViews() {
        binding = applyBinding(ActivityCodeSnippetPickerBinding.inflate(layoutInflater))
        setTitle(R.string.title_add_code_snippet)

        val manager = LinearLayoutManager(context)
        binding.sectionList.layoutManager = manager
        binding.sectionList.setHasFixedSize(true)
        binding.sectionList.adapter = adapter
        binding.sectionList.itemAnimator
    }

    private fun initUserInputBindings() {
        adapter.userEvents.observe(this) { event ->
            when (event) {
                is CodeSnippetAdapter.UserEvent.SectionClicked -> {
                    viewModel.onSectionClicked(event.id)
                }
                is CodeSnippetAdapter.UserEvent.CodeSnippetClicked -> {
                    viewModel.onCodeSnippetClicked(event.id)
                }
                is CodeSnippetAdapter.UserEvent.CodeSnippetAuxiliaryIconClicked -> {
                    viewModel.onCodeSnippetDocRefButtonClicked(event.id)
                }
            }
        }
    }

    private fun initViewModelBindings() {
        viewModel.viewState.observe(this) { viewState ->
            adapter.items = viewState.items
            setDialogState(viewState.dialogState, viewModel)
        }
        viewModel.events.observe(this, ::handleEvent)
    }

    override fun handleEvent(event: ViewModelEvent) {
        when (event) {
            is CodeSnippetPickerEvent.OpenCustomIconPicker -> {
                pickCustomIcon.launch()
            }
            is CodeSnippetPickerEvent.OpenIpackIconPicker -> {
                pickIpackIcon.launch()
            }
            is CodeSnippetPickerEvent.OpenRingtonePicker -> {
                openRingtonePicker()
            }
            is CodeSnippetPickerEvent.OpenTaskerTaskPicker -> {
                openTaskerTaskPicker()
            }
            else -> super.handleEvent(event)
        }
    }

    private fun openRingtonePicker() {
        try {
            pickRingtone.launch()
        } catch (e: ActivityNotFoundException) {
            logException(e)
            context.showToast(R.string.error_generic)
        }
    }

    private fun openTaskerTaskPicker() {
        try {
            pickTaskerTask.launch()
        } catch (e: ActivityNotFoundException) {
            logException(e)
            context.showToast(R.string.error_generic)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.scripting_activity_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_show_help -> consume { viewModel.onHelpButtonClicked() }
        else -> super.onOptionsItemSelected(item)
    }

    object PickCodeSnippet : BaseActivityResultContract<IntentBuilder, PickCodeSnippet.Result?>(::IntentBuilder) {
        override fun parseResult(resultCode: Int, intent: Intent?): Result? {
            return Result(
                textBeforeCursor = intent?.getStringExtra(EXTRA_TEXT_BEFORE_CURSOR) ?: return null,
                textAfterCursor = intent.getStringExtra(EXTRA_TEXT_AFTER_CURSOR) ?: return null,
            )
        }

        fun createResult(textBeforeCursor: String, textAfterCursor: String) =
            createIntent {
                putExtra(EXTRA_TEXT_BEFORE_CURSOR, textBeforeCursor)
                putExtra(EXTRA_TEXT_AFTER_CURSOR, textAfterCursor)
            }

        private const val EXTRA_TEXT_BEFORE_CURSOR = "text_before_cursor"
        private const val EXTRA_TEXT_AFTER_CURSOR = "text_after_cursor"

        data class Result(val textBeforeCursor: String, val textAfterCursor: String)
    }

    class IntentBuilder : BaseIntentBuilder(CodeSnippetPickerActivity::class.java) {

        fun currentShortcutId(shortcutId: ShortcutId) = also {
            intent.putExtra(EXTRA_SHORTCUT_ID, shortcutId)
        }

        fun includeResponseOptions(includeResponseOptions: Boolean) = also {
            intent.putExtra(EXTRA_INCLUDE_RESPONSE_OPTIONS, includeResponseOptions)
        }

        fun includeNetworkErrorOption(includeNetworkErrorOption: Boolean) = also {
            intent.putExtra(EXTRA_INCLUDE_NETWORK_ERROR_OPTION, includeNetworkErrorOption)
        }

        fun includeFileOptions(includeFileOptions: Boolean) = also {
            intent.putExtra(EXTRA_INCLUDE_FILE_OPTIONS, includeFileOptions)
        }
    }

    companion object {

        private const val EXTRA_SHORTCUT_ID = "shortcutId"
        private const val EXTRA_INCLUDE_RESPONSE_OPTIONS = "include_response_options"
        private const val EXTRA_INCLUDE_NETWORK_ERROR_OPTION = "include_network_error_option"
        private const val EXTRA_INCLUDE_FILE_OPTIONS = "include_file_options"
    }
}

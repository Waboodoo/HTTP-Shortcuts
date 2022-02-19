package ch.rmy.android.http_shortcuts.activities.editor.scripting

import android.app.Activity
import android.content.Intent
import android.text.SpannableStringBuilder
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import ch.rmy.android.framework.extensions.attachTo
import ch.rmy.android.framework.extensions.bindViewModel
import ch.rmy.android.framework.extensions.color
import ch.rmy.android.framework.extensions.consume
import ch.rmy.android.framework.extensions.initialize
import ch.rmy.android.framework.extensions.insertAroundCursor
import ch.rmy.android.framework.extensions.observe
import ch.rmy.android.framework.extensions.observeTextChanges
import ch.rmy.android.framework.extensions.setHint
import ch.rmy.android.framework.extensions.setTextSafely
import ch.rmy.android.framework.extensions.visible
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.framework.viewmodel.ViewModelEvent
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.databinding.ActivityScriptingBinding
import ch.rmy.android.http_shortcuts.icons.IconPicker
import ch.rmy.android.http_shortcuts.scripting.shortcuts.ShortcutPlaceholderProvider
import ch.rmy.android.http_shortcuts.scripting.shortcuts.ShortcutSpanManager
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
import ch.rmy.android.http_shortcuts.variables.Variables

class ScriptingActivity : BaseActivity() {

    private val currentShortcutId: String? by lazy {
        intent.getStringExtra(EXTRA_SHORTCUT_ID)
    }

    private val viewModel: ScriptingViewModel by bindViewModel()

    private val shortcutPlaceholderProvider = ShortcutPlaceholderProvider()
    private val variablePlaceholderProvider = VariablePlaceholderProvider()

    private val iconPicker: IconPicker by lazy {
        IconPicker(this) { icon ->
            codeSnippetPicker.insertChangeIconSnippet(
                viewModel.iconPickerShortcutPlaceholder ?: return@IconPicker,
                getCodeInsertion(lastActiveCodeInput ?: binding.inputCodePrepare),
                icon,
            )
        }
    }
    private val codeSnippetPicker by lazy {
        CodeSnippetPicker(
            context,
            currentShortcutId,
            variablePlaceholderProvider,
            shortcutPlaceholderProvider,
        ) { shortcutPlaceholder ->
            viewModel.iconPickerShortcutPlaceholder = shortcutPlaceholder
            iconPicker.openIconSelectionDialog()
        }
    }
    private val variablePlaceholderColor by lazy {
        color(context, R.color.variable)
    }
    private val shortcutPlaceholderColor by lazy {
        color(context, R.color.shortcut)
    }

    private lateinit var binding: ActivityScriptingBinding

    private var lastActiveCodeInput: EditText? = null

    override fun onCreate() {
        viewModel.initialize()
        initViews()
        initUserInputBindings()
        initViewModelBindings()
    }

    private fun initViews() {
        binding = applyBinding(ActivityScriptingBinding.inflate(layoutInflater))
        setTitle(R.string.label_scripting)
    }

    private fun initUserInputBindings() {
        binding.buttonAddCodeSnippetPre.setOnClickListener {
            viewModel.onAddCodeSnippetPrepareButtonClicked()
        }
        binding.buttonAddCodeSnippetSuccess.setOnClickListener {
            viewModel.onAddCodeSnippetSuccessButtonClicked()
        }
        binding.buttonAddCodeSnippetFailure.setOnClickListener {
            lastActiveCodeInput = binding.inputCodeFailure
            viewModel.onAddCodeSnippetFailureButtonClicked()
        }

        binding.inputCodePrepare
            .observeTextChanges()
            .subscribe {
                viewModel.onCodePrepareChanged(it.toString())
            }
            .attachTo(destroyer)
        binding.inputCodeSuccess
            .observeTextChanges()
            .subscribe {
                viewModel.onCodeSuccessChanged(it.toString())
            }
            .attachTo(destroyer)
        binding.inputCodeFailure
            .observeTextChanges()
            .subscribe {
                viewModel.onCodeFailureChanged(it.toString())
            }
            .attachTo(destroyer)
    }

    private fun processTextForView(input: String): CharSequence {
        val text = SpannableStringBuilder(input)
        Variables.applyVariableFormattingToJS(
            text,
            variablePlaceholderProvider,
            variablePlaceholderColor,
        )
        ShortcutSpanManager.applyShortcutFormattingToJS(
            text,
            shortcutPlaceholderProvider,
            shortcutPlaceholderColor,
        )
        return text
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.scripting_activity_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_show_help -> consume { viewModel.onHelpButtonClicked() }
        else -> super.onOptionsItemSelected(item)
    }

    private fun initViewModelBindings() {
        viewModel.viewState.observe(this) { viewState ->
            viewState.variables?.let(variablePlaceholderProvider::applyVariables)
            binding.inputCodePrepare.minLines = viewState.codePrepareMinLines
            binding.inputCodePrepare.setHint(viewState.codePrepareHint)
            binding.labelCodePrepare.visible = viewState.codePrepareVisible
            binding.containerPostRequestScripting.visible = viewState.postRequestScriptingVisible
            binding.inputCodeSuccess.setTextSafely(processTextForView(viewState.codeOnSuccess))
            binding.inputCodeFailure.setTextSafely(processTextForView(viewState.codeOnFailure))
            binding.inputCodePrepare.setTextSafely(processTextForView(viewState.codeOnPrepare))
            shortcutPlaceholderProvider.applyShortcuts(viewState.shortcuts)
        }
        viewModel.events.observe(this, ::handleEvent)
    }

    override fun handleEvent(event: ViewModelEvent) {
        when (event) {
            is ScriptingEvent.ShowCodeSnippetPicker -> {
                val view = when (event.target) {
                    ScriptingEvent.ShowCodeSnippetPicker.Target.PREPARE -> {
                        binding.inputCodePrepare
                    }
                    ScriptingEvent.ShowCodeSnippetPicker.Target.SUCCESS -> {
                        binding.inputCodeSuccess
                    }
                    ScriptingEvent.ShowCodeSnippetPicker.Target.FAILURE -> {
                        binding.inputCodeFailure
                    }
                }
                showCodeSnippetPicker(
                    view,
                    includeFileOptions = event.includeFileOptions,
                    includeResponseOptions = event.includeResponseOptions,
                    includeNetworkErrorOption = event.includeNetworkErrorOption,
                )
            }
            else -> super.handleEvent(event)
        }
    }

    private fun showCodeSnippetPicker(
        editText: EditText,
        includeFileOptions: Boolean,
        includeResponseOptions: Boolean,
        includeNetworkErrorOption: Boolean,
    ) {
        lastActiveCodeInput = editText
        codeSnippetPicker.showCodeSnippetPicker(
            getCodeInsertion(editText),
            includeResponseOptions = includeResponseOptions,
            includeFileOptions = includeFileOptions,
            includeNetworkErrorOption = includeNetworkErrorOption,
        )
    }

    private fun getCodeInsertion(codeInput: EditText): InsertText =
        { before, after ->
            codeInput.insertAroundCursor(before, after)
            Variables.applyVariableFormattingToJS(codeInput.text, variablePlaceholderProvider, variablePlaceholderColor)
            ShortcutSpanManager.applyShortcutFormattingToJS(codeInput.text, shortcutPlaceholderProvider, shortcutPlaceholderColor)
        }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            codeSnippetPicker.handleRequestResult(
                getCodeInsertion(lastActiveCodeInput ?: binding.inputCodePrepare),
                requestCode,
                data,
            )
        }
        iconPicker.handleResult(requestCode, resultCode, data)
    }

    override fun onBackPressed() {
        viewModel.onBackPressed()
    }

    class IntentBuilder : BaseIntentBuilder(ScriptingActivity::class.java) {

        fun shortcutId(shortcutId: String?) = also {
            intent.putExtra(EXTRA_SHORTCUT_ID, shortcutId)
        }
    }

    companion object {
        private const val EXTRA_SHORTCUT_ID = "shortcutId"
    }
}

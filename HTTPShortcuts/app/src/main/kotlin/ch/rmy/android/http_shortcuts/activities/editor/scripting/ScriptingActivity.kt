package ch.rmy.android.http_shortcuts.activities.editor.scripting

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.Menu
import android.view.MenuItem
import androidx.core.view.isVisible
import ch.rmy.android.framework.extensions.attachTo
import ch.rmy.android.framework.extensions.bindViewModel
import ch.rmy.android.framework.extensions.color
import ch.rmy.android.framework.extensions.consume
import ch.rmy.android.framework.extensions.initialize
import ch.rmy.android.framework.extensions.insertAroundCursor
import ch.rmy.android.framework.extensions.observe
import ch.rmy.android.framework.extensions.observeTextChanges
import ch.rmy.android.framework.extensions.runIfNotNull
import ch.rmy.android.framework.extensions.setHint
import ch.rmy.android.framework.extensions.setTextSafely
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.framework.viewmodel.ViewModelEvent
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.activities.editor.scripting.codesnippets.CodeSnippetPickerActivity
import ch.rmy.android.http_shortcuts.dagger.ApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.databinding.ActivityScriptingBinding
import ch.rmy.android.http_shortcuts.scripting.shortcuts.ShortcutPlaceholderProvider
import ch.rmy.android.http_shortcuts.scripting.shortcuts.ShortcutSpanManager
import ch.rmy.android.http_shortcuts.utils.InvalidSpanRemover
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
import ch.rmy.android.http_shortcuts.variables.Variables
import javax.inject.Inject

class ScriptingActivity : BaseActivity() {

    @Inject
    lateinit var variablePlaceholderProvider: VariablePlaceholderProvider

    @Inject
    lateinit var shortcutPlaceholderProvider: ShortcutPlaceholderProvider

    private val pickCodeSnippetForPrepare = registerForActivityResult(CodeSnippetPickerActivity.PickCodeSnippet) { result ->
        if (result != null) {
            viewModel.onCodeSnippetForPreparePicked(result.textBeforeCursor, result.textAfterCursor)
        }
    }
    private val pickCodeSnippetForSuccess = registerForActivityResult(CodeSnippetPickerActivity.PickCodeSnippet) { result ->
        if (result != null) {
            viewModel.onCodeSnippetForSuccessPicked(result.textBeforeCursor, result.textAfterCursor)
        }
    }
    private val pickCodeSnippetForFailure = registerForActivityResult(CodeSnippetPickerActivity.PickCodeSnippet) { result ->
        if (result != null) {
            viewModel.onCodeSnippetForFailurePicked(result.textBeforeCursor, result.textAfterCursor)
        }
    }

    private val currentShortcutId: ShortcutId? by lazy(LazyThreadSafetyMode.NONE) {
        intent.getStringExtra(EXTRA_SHORTCUT_ID)
    }

    private val viewModel: ScriptingViewModel by bindViewModel()

    private val variablePlaceholderColor by lazy(LazyThreadSafetyMode.NONE) {
        color(context, R.color.variable)
    }
    private val shortcutPlaceholderColor by lazy(LazyThreadSafetyMode.NONE) {
        color(context, R.color.shortcut)
    }

    private lateinit var binding: ActivityScriptingBinding

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }

    override fun onCreated(savedState: Bundle?) {
        viewModel.initialize()
        initViews()
        initUserInputBindings()
        initViewModelBindings()
    }

    private fun initViews() {
        binding = applyBinding(ActivityScriptingBinding.inflate(layoutInflater))
        binding.inputCodePrepare.addTextChangedListener(InvalidSpanRemover())
        binding.inputCodeSuccess.addTextChangedListener(InvalidSpanRemover())
        binding.inputCodeFailure.addTextChangedListener(InvalidSpanRemover())
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
            viewState.shortcuts?.let(shortcutPlaceholderProvider::applyShortcuts)
            binding.inputCodePrepare.minLines = viewState.codePrepareMinLines
            binding.inputCodePrepare.setHint(viewState.codePrepareHint)
            binding.labelCodePrepare.isVisible = viewState.codePrepareVisible
            binding.containerPostRequestScripting.isVisible = viewState.postRequestScriptingVisible
            binding.inputCodeSuccess.setTextSafely(processTextForView(viewState.codeOnSuccess))
            binding.inputCodeFailure.setTextSafely(processTextForView(viewState.codeOnFailure))
            binding.inputCodePrepare.setTextSafely(processTextForView(viewState.codeOnPrepare))
            setDialogState(viewState.dialogState, viewModel)
        }
        viewModel.events.observe(this, ::handleEvent)
    }

    override fun handleEvent(event: ViewModelEvent) {
        when (event) {
            is ScriptingEvent.ShowCodeSnippetPicker -> {
                showCodeSnippetPicker(
                    event.target,
                    includeFileOptions = event.includeFileOptions,
                    includeResponseOptions = event.includeResponseOptions,
                    includeNetworkErrorOption = event.includeNetworkErrorOption,
                )
            }
            is ScriptingEvent.InsertCodeSnippet -> {
                insertCodeSnippet(
                    event.target,
                    event.textBeforeCursor,
                    event.textAfterCursor,
                )
            }
            else -> super.handleEvent(event)
        }
    }

    private fun showCodeSnippetPicker(
        target: TargetCodeFieldType,
        includeFileOptions: Boolean,
        includeResponseOptions: Boolean,
        includeNetworkErrorOption: Boolean,
    ) {
        val applyArguments: ((CodeSnippetPickerActivity.IntentBuilder) -> CodeSnippetPickerActivity.IntentBuilder) =
            { intentBuilder: CodeSnippetPickerActivity.IntentBuilder ->
                intentBuilder
                    .runIfNotNull(currentShortcutId) {
                        currentShortcutId(it)
                    }
                    .includeFileOptions(includeFileOptions)
                    .includeResponseOptions(includeResponseOptions)
                    .includeNetworkErrorOption(includeNetworkErrorOption)
            }
        when (target) {
            TargetCodeFieldType.PREPARE -> pickCodeSnippetForPrepare.launch(applyArguments)
            TargetCodeFieldType.SUCCESS -> pickCodeSnippetForSuccess.launch(applyArguments)
            TargetCodeFieldType.FAILURE -> pickCodeSnippetForFailure.launch(applyArguments)
        }
    }

    private fun insertCodeSnippet(target: TargetCodeFieldType, textBeforeCursor: String, textAfterCursor: String) {
        val codeInput = when (target) {
            TargetCodeFieldType.PREPARE -> binding.inputCodePrepare
            TargetCodeFieldType.SUCCESS -> binding.inputCodeSuccess
            TargetCodeFieldType.FAILURE -> binding.inputCodeFailure
        }
        codeInput.insertAroundCursor(textBeforeCursor, textAfterCursor)
        codeInput.text?.let {
            Variables.applyVariableFormattingToJS(it, variablePlaceholderProvider, variablePlaceholderColor)
            ShortcutSpanManager.applyShortcutFormattingToJS(it, shortcutPlaceholderProvider, shortcutPlaceholderColor)
        }
    }

    override fun onBackPressed() {
        viewModel.onBackPressed()
    }

    class IntentBuilder : BaseIntentBuilder(ScriptingActivity::class) {

        fun shortcutId(shortcutId: ShortcutId?) = also {
            intent.putExtra(EXTRA_SHORTCUT_ID, shortcutId)
        }
    }

    companion object {
        private const val EXTRA_SHORTCUT_ID = "shortcutId"
    }
}

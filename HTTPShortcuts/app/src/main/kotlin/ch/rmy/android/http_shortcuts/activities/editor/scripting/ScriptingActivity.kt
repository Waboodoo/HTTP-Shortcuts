package ch.rmy.android.http_shortcuts.activities.editor.scripting

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.data.enums.ShortcutExecutionType
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.databinding.ActivityScriptingBinding
import ch.rmy.android.http_shortcuts.extensions.attachTo
import ch.rmy.android.http_shortcuts.extensions.bindViewModel
import ch.rmy.android.http_shortcuts.extensions.color
import ch.rmy.android.http_shortcuts.extensions.consume
import ch.rmy.android.http_shortcuts.extensions.insertAroundCursor
import ch.rmy.android.http_shortcuts.extensions.observeTextChanges
import ch.rmy.android.http_shortcuts.extensions.openURL
import ch.rmy.android.http_shortcuts.extensions.setTextSafely
import ch.rmy.android.http_shortcuts.extensions.type
import ch.rmy.android.http_shortcuts.extensions.visible
import ch.rmy.android.http_shortcuts.icons.IconPicker
import ch.rmy.android.http_shortcuts.scripting.shortcuts.ShortcutPlaceholderProvider
import ch.rmy.android.http_shortcuts.scripting.shortcuts.ShortcutSpanManager
import ch.rmy.android.http_shortcuts.utils.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.utils.ExternalURLs
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
import ch.rmy.android.http_shortcuts.variables.Variables
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

class ScriptingActivity : BaseActivity() {

    private val currentShortcutId: String? by lazy {
        intent.getStringExtra(EXTRA_SHORTCUT_ID)
    }

    private val viewModel: ScriptingViewModel by bindViewModel()
    private val shortcutData by lazy {
        viewModel.shortcut
    }
    private val variablesData by lazy {
        viewModel.variables
    }
    private val variablePlaceholderProvider by lazy {
        VariablePlaceholderProvider(variablesData)
    }
    private val shortcutsData by lazy {
        viewModel.shortcuts
    }
    private val shortcutPlaceholderProvider by lazy {
        ShortcutPlaceholderProvider(shortcutsData)
    }
    private val iconPicker: IconPicker by lazy {
        IconPicker(this) { icon ->
            Completable.fromAction {
                codeSnippetPicker.insertChangeIconSnippet(
                    viewModel.iconPickerShortcutPlaceholder ?: return@fromAction,
                    getCodeInsertion(lastActiveCodeInput ?: return@fromAction),
                    icon,
                )
            }
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = applyBinding(ActivityScriptingBinding.inflate(layoutInflater))
        setTitle(R.string.label_scripting)

        initViews()
        bindViewsToViewModel()
    }

    private fun initViews() {
        binding.buttonAddCodeSnippetPre.setOnClickListener {
            lastActiveCodeInput = binding.inputCodePrepare
            codeSnippetPicker.showCodeSnippetPicker(
                getCodeInsertion(binding.inputCodePrepare),
                includeResponseOptions = false,
                includeFileOptions = shortcutData.value?.type != ShortcutExecutionType.SCRIPTING,
            )
        }
        binding.buttonAddCodeSnippetSuccess.setOnClickListener {
            lastActiveCodeInput = binding.inputCodeSuccess
            codeSnippetPicker.showCodeSnippetPicker(getCodeInsertion(binding.inputCodeSuccess))
        }
        binding.buttonAddCodeSnippetFailure.setOnClickListener {
            lastActiveCodeInput = binding.inputCodeFailure
            codeSnippetPicker.showCodeSnippetPicker(getCodeInsertion(binding.inputCodeFailure), includeNetworkErrorOption = true)
        }
    }

    private fun getCodeInsertion(codeInput: EditText): InsertText =
        { before, after ->
            codeInput.insertAroundCursor(before, after)
            Variables.applyVariableFormattingToJS(codeInput.text, variablePlaceholderProvider, variablePlaceholderColor)
            ShortcutSpanManager.applyShortcutFormattingToJS(codeInput.text, shortcutPlaceholderProvider, shortcutPlaceholderColor)
        }

    private fun bindViewsToViewModel() {
        shortcutData.observe(this) {
            val shortcut = shortcutData.value ?: return@observe
            updateShortcutViews(shortcut)
            shortcutData.removeObservers(this)
        }
        bindTextChangeListener(binding.inputCodePrepare) { shortcutData.value?.codeOnPrepare }
        bindTextChangeListener(binding.inputCodeSuccess) { shortcutData.value?.codeOnSuccess }
        bindTextChangeListener(binding.inputCodeFailure) { shortcutData.value?.codeOnFailure }
    }

    private fun bindTextChangeListener(textView: EditText, currentValueProvider: () -> String?) {
        textView.observeTextChanges()
            .debounce(300, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .filter { it != currentValueProvider.invoke() }
            .concatMapCompletable { updateViewModelFromViews() }
            .subscribe()
            .attachTo(destroyer)
    }

    private fun updateViewModelFromViews(): Completable =
        viewModel.setCode(
            prepareCode = binding.inputCodePrepare.text.toString(),
            successCode = binding.inputCodeSuccess.text.toString(),
            failureCode = binding.inputCodeFailure.text.toString(),
        )

    private fun updateShortcutViews(shortcut: Shortcut) {
        val type = shortcut.type
        binding.inputCodePrepare.minLines = getMinLinesForCode(type)
        binding.inputCodePrepare.setHint(getHintText(type))
        binding.labelCodePrepare.visible = type != ShortcutExecutionType.SCRIPTING
        binding.containerPostRequestScripting.visible = type.usesResponse
        binding.inputCodeSuccess.setTextSafely(processTextForView(shortcut.codeOnSuccess))
        binding.inputCodeFailure.setTextSafely(processTextForView(shortcut.codeOnFailure))
        binding.inputCodePrepare.setTextSafely(processTextForView(shortcut.codeOnPrepare))
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

    override fun onBackPressed() {
        updateViewModelFromViews()
            .subscribe {
                finish()
            }
            .attachTo(destroyer)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.scripting_activity_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_show_help -> consume { showHelp() }
        else -> super.onOptionsItemSelected(item)
    }

    private fun showHelp() {
        openURL(ExternalURLs.SCRIPTING_DOCUMENTATION)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            codeSnippetPicker.handleRequestResult(
                getCodeInsertion(lastActiveCodeInput ?: return),
                requestCode,
                data,
            )
        }
        iconPicker.handleResult(requestCode, resultCode, data)
    }

    class IntentBuilder(context: Context) : BaseIntentBuilder(context, ScriptingActivity::class.java) {

        fun shortcutId(shortcutId: String?) = also {
            intent.putExtra(EXTRA_SHORTCUT_ID, shortcutId)
        }
    }

    companion object {
        private const val EXTRA_SHORTCUT_ID = "shortcutId"

        private fun getMinLinesForCode(type: ShortcutExecutionType) = if (type == ShortcutExecutionType.SCRIPTING) {
            18
        } else {
            6
        }

        private fun getHintText(type: ShortcutExecutionType) = if (type == ShortcutExecutionType.SCRIPTING) {
            R.string.placeholder_javascript_code_generic
        } else {
            R.string.placeholder_javascript_code_before
        }
    }
}

package ch.rmy.android.http_shortcuts.activities.editor.scripting

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.lifecycle.Observer
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.data.enums.ShortcutExecutionType
import ch.rmy.android.http_shortcuts.data.models.Shortcut
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
import ch.rmy.android.http_shortcuts.scripting.shortcuts.ShortcutPlaceholderProvider
import ch.rmy.android.http_shortcuts.scripting.shortcuts.ShortcutSpanManager
import ch.rmy.android.http_shortcuts.utils.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
import ch.rmy.android.http_shortcuts.variables.Variables
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import kotterknife.bindView
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
    private val codeSnippetPicker by lazy {
        destroyer.own(CodeSnippetPicker(
            context,
            currentShortcutId,
            variablePlaceholderProvider,
            shortcutPlaceholderProvider
        ))
    }
    private val variablePlaceholderColor by lazy {
        color(context, R.color.variable)
    }
    private val shortcutPlaceholderColor by lazy {
        color(context, R.color.shortcut)
    }

    private val labelPrepareCode: View by bindView(R.id.label_code_prepare)
    private val prepareCodeInput: EditText by bindView(R.id.input_code_prepare)
    private val prepareSnippetButton: Button by bindView(R.id.button_add_code_snippet_pre)
    private val successCodeInput: EditText by bindView(R.id.input_code_success)
    private val failureCodeInput: EditText by bindView(R.id.input_code_failure)
    private val successSnippetButton: Button by bindView(R.id.button_add_code_snippet_success)
    private val failureSnippetButton: Button by bindView(R.id.button_add_code_snippet_failure)
    private val postRequestContainer: View by bindView(R.id.container_post_request_scripting)

    private var lastActiveCodeInput: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scripting)
        setTitle(R.string.label_scripting)

        initViews()
        bindViewsToViewModel()
    }

    private fun initViews() {
        prepareSnippetButton.setOnClickListener {
            lastActiveCodeInput = prepareCodeInput
            codeSnippetPicker.showCodeSnippetPicker(getCodeInsertion(prepareCodeInput), includeResponseOptions = false)
        }
        successSnippetButton.setOnClickListener {
            lastActiveCodeInput = successCodeInput
            codeSnippetPicker.showCodeSnippetPicker(getCodeInsertion(successCodeInput))
        }
        failureSnippetButton.setOnClickListener {
            lastActiveCodeInput = failureCodeInput
            codeSnippetPicker.showCodeSnippetPicker(getCodeInsertion(failureCodeInput), includeNetworkErrorOption = true)
        }
    }

    private fun getCodeInsertion(codeInput: EditText): (String, String) -> Unit =
        { before, after ->
            codeInput.insertAroundCursor(before, after)
            Variables.applyVariableFormattingToJS(codeInput.text, variablePlaceholderProvider, variablePlaceholderColor)
            ShortcutSpanManager.applyShortcutFormattingToJS(codeInput.text, shortcutPlaceholderProvider, shortcutPlaceholderColor)
        }

    private fun bindViewsToViewModel() {
        shortcutData.observe(this, Observer {
            val shortcut = shortcutData.value ?: return@Observer
            updateShortcutViews(shortcut)
            shortcutData.removeObservers(this)
        })
        bindTextChangeListener(prepareCodeInput) { shortcutData.value?.codeOnPrepare }
        bindTextChangeListener(successCodeInput) { shortcutData.value?.codeOnSuccess }
        bindTextChangeListener(failureCodeInput) { shortcutData.value?.codeOnFailure }
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
            prepareCode = prepareCodeInput.text.toString(),
            successCode = successCodeInput.text.toString(),
            failureCode = failureCodeInput.text.toString()
        )

    private fun updateShortcutViews(shortcut: Shortcut) {
        val type = shortcut.type
        prepareCodeInput.minLines = getMinLinesForCode(type)
        prepareCodeInput.setHint(getHintText(type))
        labelPrepareCode.visible = type != ShortcutExecutionType.SCRIPTING
        postRequestContainer.visible = type.usesResponse
        successCodeInput.setTextSafely(processTextForView(shortcut.codeOnSuccess))
        failureCodeInput.setTextSafely(processTextForView(shortcut.codeOnFailure))
        prepareCodeInput.setTextSafely(processTextForView(shortcut.codeOnPrepare))
    }

    private fun processTextForView(input: String): CharSequence {
        val text = SpannableStringBuilder(input)
        Variables.applyVariableFormattingToJS(
            text,
            variablePlaceholderProvider,
            variablePlaceholderColor
        )
        ShortcutSpanManager.applyShortcutFormattingToJS(
            text,
            shortcutPlaceholderProvider,
            shortcutPlaceholderColor
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
        context.openURL(CODE_HELP_URL)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            codeSnippetPicker.handleRequestResult(
                getCodeInsertion(lastActiveCodeInput ?: return),
                requestCode,
                data
            )
        }
    }

    class IntentBuilder(context: Context) : BaseIntentBuilder(context, ScriptingActivity::class.java) {

        fun shortcutId(shortcutId: String?) = also {
            intent.putExtra(EXTRA_SHORTCUT_ID, shortcutId)
        }

    }

    companion object {
        private const val EXTRA_SHORTCUT_ID = "shortcutId"

        private const val CODE_HELP_URL = "https://http-shortcuts.rmy.ch/scripting"

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
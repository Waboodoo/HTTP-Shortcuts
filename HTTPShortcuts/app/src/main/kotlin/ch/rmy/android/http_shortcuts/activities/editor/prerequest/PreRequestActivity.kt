package ch.rmy.android.http_shortcuts.activities.editor.prerequest

import android.content.Context
import android.os.Bundle
import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.widget.Button
import android.widget.EditText
import androidx.lifecycle.Observer
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.actions.ActionsUtil
import ch.rmy.android.http_shortcuts.actions.types.ActionFactory
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.activities.editor.CodeSnippetPicker
import ch.rmy.android.http_shortcuts.extensions.attachTo
import ch.rmy.android.http_shortcuts.extensions.bindViewModel
import ch.rmy.android.http_shortcuts.extensions.color
import ch.rmy.android.http_shortcuts.extensions.insertAroundCursor
import ch.rmy.android.http_shortcuts.extensions.observeTextChanges
import ch.rmy.android.http_shortcuts.extensions.setTextSafely
import ch.rmy.android.http_shortcuts.utils.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
import ch.rmy.android.http_shortcuts.variables.Variables
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import kotterknife.bindView
import java.util.concurrent.TimeUnit

class PreRequestActivity : BaseActivity() {

    private val viewModel: PreRequestViewModel by bindViewModel()
    private val shortcutData by lazy {
        viewModel.shortcut
    }
    private val variablesData by lazy {
        viewModel.variables
    }
    private val variablePlaceholderProvider by lazy {
        VariablePlaceholderProvider(variablesData)
    }
    private val actionFactory by lazy {
        ActionFactory(context)
    }
    private val codeSnippetPicker by lazy {
        CodeSnippetPicker(context, variablePlaceholderProvider)
    }
    private val variablePlaceholderColor by lazy {
        color(context, R.color.variable)
    }

    private val prepareCodeInput: EditText by bindView(R.id.input_code_prepare)
    private val prepareSnippetButton: Button by bindView(R.id.button_add_code_snippet_pre)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pre_request)

        initViews()
        bindViewsToViewModel()
    }

    private fun initViews() {
        prepareCodeInput.movementMethod = LinkMovementMethod.getInstance()

        prepareSnippetButton.setOnClickListener {
            codeSnippetPicker.showCodeSnippetPicker({ before, after ->
                prepareCodeInput.insertAroundCursor(before, after)
                Variables.applyVariableFormattingToJS(prepareCodeInput.text, variablePlaceholderProvider, variablePlaceholderColor)
            }, includeResponseOptions = false)
        }
    }

    private fun bindViewsToViewModel() {
        shortcutData.observe(this, Observer {
            updateShortcutViews()
        })
        bindTextChangeListener(prepareCodeInput) { shortcutData.value?.codeOnPrepare }
    }

    private fun bindTextChangeListener(textView: EditText, currentValueProvider: () -> String?) {
        textView.observeTextChanges()
            .debounce(300, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .map {
                ActionsUtil.removeSpans(it as Spannable)
            }
            .filter { it != currentValueProvider.invoke() }
            .concatMapCompletable { updateViewModelFromViews() }
            .subscribe()
            .attachTo(destroyer)
    }

    private fun updateViewModelFromViews(): Completable =
        viewModel.setCodeOnPrepare(
            code = ActionsUtil.removeSpans(prepareCodeInput.text)
        )

    private fun updateShortcutViews() {
        val shortcut = shortcutData.value ?: return
        prepareCodeInput.setTextSafely(processTextForView(shortcut.codeOnPrepare))
    }

    private fun processTextForView(input: String): CharSequence {
        val text = ActionsUtil.addSpans(
            context,
            input,
            actionFactory
        )
        Variables.applyVariableFormattingToJS(text, variablePlaceholderProvider, variablePlaceholderColor)
        return text
    }

    override fun onBackPressed() {
        updateViewModelFromViews()
            .subscribe {
                finish()
            }
            .attachTo(destroyer)
    }

    class IntentBuilder(context: Context) : BaseIntentBuilder(context, PreRequestActivity::class.java)

}
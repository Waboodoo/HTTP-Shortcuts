package ch.rmy.android.http_shortcuts.activities.editor.postrequest

import android.content.Context
import android.os.Bundle
import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.widget.Button
import android.widget.EditText
import androidx.lifecycle.Observer
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.actions.ActionDTO
import ch.rmy.android.http_shortcuts.actions.ActionsUtil
import ch.rmy.android.http_shortcuts.actions.types.ActionFactory
import ch.rmy.android.http_shortcuts.actions.types.BaseAction
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.activities.editor.CodeSnippetPicker
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.extensions.attachTo
import ch.rmy.android.http_shortcuts.extensions.bindViewModel
import ch.rmy.android.http_shortcuts.extensions.observeTextChanges
import ch.rmy.android.http_shortcuts.extensions.setTextSafely
import ch.rmy.android.http_shortcuts.utils.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
import ch.rmy.android.http_shortcuts.views.LabelledSpinner
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import kotterknife.bindView
import java.util.concurrent.TimeUnit

class PostRequestActivity : BaseActivity() {

    private val viewModel: PostRequestViewModel by bindViewModel()
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

    private val feedbackTypeSpinner: LabelledSpinner by bindView(R.id.input_feedback_type)
    private val successCodeInput: EditText by bindView(R.id.input_code_success)
    private val failureCodeInput: EditText by bindView(R.id.input_code_failure)
    private val successSnippetButton: Button by bindView(R.id.button_add_code_snippet_success)
    private val failureSnippetButton: Button by bindView(R.id.button_add_code_snippet_failure)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_request)

        initViews()
        bindViewsToViewModel()
    }

    private fun initViews() {
        feedbackTypeSpinner.setItemsFromPairs(REQUEST_BODY_TYPES.map {
            it.first to getString(it.second)
        })

        successCodeInput.movementMethod = LinkMovementMethod.getInstance()
        failureCodeInput.movementMethod = LinkMovementMethod.getInstance()

        successSnippetButton.setOnClickListener {
            codeSnippetPicker.showCodeSnippetPicker(successCodeInput)
        }
        failureSnippetButton.setOnClickListener {
            codeSnippetPicker.showCodeSnippetPicker(failureCodeInput, includeNetworkErrorOption = true)
        }
    }

    private fun bindViewsToViewModel() {
        shortcutData.observe(this, Observer {
            updateShortcutViews()
        })
        bindTextChangeListener(successCodeInput) { shortcutData.value?.codeOnSuccess }
        bindTextChangeListener(failureCodeInput) { shortcutData.value?.codeOnFailure }

        feedbackTypeSpinner.selectionChanges
            .concatMapCompletable { type -> viewModel.setFeedbackType(type) }
            .subscribe()
            .attachTo(destroyer)
    }

    private fun bindTextChangeListener(textView: EditText, currentValueProvider: () -> String?) {
        textView.observeTextChanges()
            .debounce(300, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .map {
                ActionsUtil.removeSpans(it as Spannable)
            }
            .filter { it.toString() != currentValueProvider.invoke() }
            .concatMapCompletable { updateViewModelFromViews() }
            .subscribe()
            .attachTo(destroyer)
    }

    private fun updateViewModelFromViews(): Completable =
        viewModel.setCode(
            successCode = ActionsUtil.removeSpans(successCodeInput.text),
            failureCode = ActionsUtil.removeSpans(failureCodeInput.text)
        )

    private fun updateShortcutViews() {
        val shortcut = shortcutData.value ?: return
        successCodeInput.setTextSafely(
            ActionsUtil.addSpans(
                context,
                shortcut.codeOnSuccess,
                actionFactory,
                ::editAction
            )
        )
        failureCodeInput.setTextSafely(
            ActionsUtil.addSpans(
                context,
                shortcut.codeOnFailure,
                actionFactory,
                ::editAction
            )
        )
        feedbackTypeSpinner.selectedItem = shortcut.feedback
    }

    private fun editAction(action: BaseAction, setter: (ActionDTO) -> Unit) {
        action.edit(context, variablePlaceholderProvider)
            .subscribe(
                {
                    setter(action.toDTO())
                    updateViewModelFromViews()
                        .subscribe()
                        .attachTo(destroyer)
                },
                {}
            )
            .attachTo(destroyer)
    }

    class IntentBuilder(context: Context) : BaseIntentBuilder(context, PostRequestActivity::class.java)

    companion object {

        private val REQUEST_BODY_TYPES = listOf(
            Shortcut.FEEDBACK_NONE to R.string.feedback_none,
            Shortcut.FEEDBACK_TOAST_SIMPLE to R.string.feedback_simple_toast,
            Shortcut.FEEDBACK_TOAST_SIMPLE_ERRORS to R.string.feedback_simple_toast_error,
            Shortcut.FEEDBACK_TOAST_ERRORS to R.string.feedback_response_toast_error,
            Shortcut.FEEDBACK_TOAST to R.string.feedback_response_toast,
            Shortcut.FEEDBACK_DIALOG to R.string.feedback_dialog,
            Shortcut.FEEDBACK_ACTIVITY to R.string.feedback_activity
        )

    }

}
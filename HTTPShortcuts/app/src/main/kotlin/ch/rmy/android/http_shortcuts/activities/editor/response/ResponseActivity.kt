package ch.rmy.android.http_shortcuts.activities.editor.response

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.Observer
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.extensions.attachTo
import ch.rmy.android.http_shortcuts.extensions.bindViewModel
import ch.rmy.android.http_shortcuts.utils.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.views.LabelledSpinner
import kotterknife.bindView

class ResponseActivity : BaseActivity() {

    private val viewModel: ResponseViewModel by bindViewModel()
    private val shortcutData by lazy {
        viewModel.shortcut
    }

    private val feedbackTypeSpinner: LabelledSpinner by bindView(R.id.input_feedback_type)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_response)

        initViews()
        bindViewsToViewModel()
    }

    private fun initViews() {
        feedbackTypeSpinner.setItemsFromPairs(REQUEST_BODY_TYPES.map {
            it.first to getString(it.second)
        })
    }

    private fun bindViewsToViewModel() {
        shortcutData.observe(this, Observer {
            updateShortcutViews()
        })

        feedbackTypeSpinner.selectionChanges
            .concatMapCompletable { type -> viewModel.setFeedbackType(type) }
            .subscribe()
            .attachTo(destroyer)
    }

    private fun updateShortcutViews() {
        val shortcut = shortcutData.value ?: return
        feedbackTypeSpinner.selectedItem = shortcut.feedback
    }

    class IntentBuilder(context: Context) : BaseIntentBuilder(context, ResponseActivity::class.java)

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
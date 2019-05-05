package ch.rmy.android.http_shortcuts.activities.editor.postrequest

import android.content.Context
import android.os.Bundle
import android.widget.EditText
import androidx.lifecycle.Observer
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.extensions.attachTo
import ch.rmy.android.http_shortcuts.extensions.bindViewModel
import ch.rmy.android.http_shortcuts.extensions.observeTextChanges
import ch.rmy.android.http_shortcuts.extensions.setTextSafely
import ch.rmy.android.http_shortcuts.utils.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
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

    private val successCodeInput: EditText by bindView(R.id.input_code_success)
    private val failureCodeInput: EditText by bindView(R.id.input_code_failure)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_request)

        initViews()
        bindViewsToViewModel()
    }

    private fun initViews() {

    }

    private fun bindViewsToViewModel() {
        shortcutData.observe(this, Observer {
            updateShortcutViews()
        })
        bindTextChangeListener(successCodeInput) { shortcutData.value?.codeOnSuccess }
        bindTextChangeListener(failureCodeInput) { shortcutData.value?.codeOnFailure }
    }

    private fun bindTextChangeListener(textView: EditText, currentValueProvider: () -> String?) {
        textView.observeTextChanges()
            .debounce(300, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .filter { it.toString() != currentValueProvider.invoke() }
            .concatMapCompletable { updateViewModelFromViews() }
            .subscribe()
            .attachTo(destroyer)
    }

    private fun updateViewModelFromViews(): Completable =
        viewModel.setCode(
            successCode = successCodeInput.text.toString(),
            failureCode = failureCodeInput.text.toString()
        )

    private fun updateShortcutViews() {
        val shortcut = shortcutData.value ?: return
        successCodeInput.setTextSafely(shortcut.codeOnSuccess)
        failureCodeInput.setTextSafely(shortcut.codeOnFailure)
    }

    class IntentBuilder(context: Context) : BaseIntentBuilder(context, PostRequestActivity::class.java)

}
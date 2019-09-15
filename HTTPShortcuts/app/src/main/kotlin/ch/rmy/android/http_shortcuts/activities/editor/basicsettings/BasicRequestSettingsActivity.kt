package ch.rmy.android.http_shortcuts.activities.editor.basicsettings

import android.content.Context
import android.os.Bundle
import android.widget.EditText
import androidx.lifecycle.Observer
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.extensions.attachTo
import ch.rmy.android.http_shortcuts.extensions.bindViewModel
import ch.rmy.android.http_shortcuts.extensions.observeTextChanges
import ch.rmy.android.http_shortcuts.extensions.visible
import ch.rmy.android.http_shortcuts.utils.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.variables.VariableButton
import ch.rmy.android.http_shortcuts.variables.VariableEditText
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
import ch.rmy.android.http_shortcuts.variables.VariableViewUtils.bindVariableViews
import ch.rmy.android.http_shortcuts.views.LabelledSpinner
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import kotterknife.bindView
import java.util.concurrent.TimeUnit

class BasicRequestSettingsActivity : BaseActivity() {

    private val viewModel: BasicRequestSettingsViewModel by bindViewModel()
    private val shortcutData by lazy {
        viewModel.shortcut
    }
    private val variablesData by lazy {
        viewModel.variables
    }
    private val variablePlaceholderProvider by lazy {
        VariablePlaceholderProvider(variablesData)
    }

    private val methodSpinner: LabelledSpinner by bindView(R.id.input_method)
    private val urlView: VariableEditText by bindView(R.id.input_url)
    private val urlVariableButton: VariableButton by bindView(R.id.variable_button_url)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_basic_request_settings)

        initViews()
        bindViewsToViewModel()
    }

    private fun initViews() {
        methodSpinner.setItemsFromPairs(METHODS.map {
            it to it
        })
        bindVariableViews(urlView, urlVariableButton, variablePlaceholderProvider)
            .attachTo(destroyer)
    }

    private fun bindViewsToViewModel() {
        shortcutData.observe(this, Observer {
            updateShortcutViews()
        })
        variablesData.observe(this, Observer {
            updateShortcutViews()
        })

        methodSpinner.selectionChanges
            .concatMapCompletable { method -> viewModel.setMethod(method) }
            .subscribe()
            .attachTo(destroyer)
        bindTextChangeListener(urlView) { shortcutData.value?.url }
    }

    private fun bindTextChangeListener(textView: EditText, currentValueProvider: () -> String?) {
        textView.observeTextChanges()
            .debounce(300, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .filter { it.toString() != currentValueProvider.invoke() }
            .concatMapCompletable { updateViewModelFromUrlView() }
            .subscribe()
            .attachTo(destroyer)
    }

    private fun updateViewModelFromUrlView(): Completable =
        viewModel.setUrl(urlView.rawString)

    private fun updateShortcutViews() {
        val shortcut = shortcutData.value ?: return

        methodSpinner.visible = !shortcut.isBrowserShortcut
        methodSpinner.selectedItem = shortcut.method
        urlView.rawString = shortcut.url
    }

    override fun onBackPressed() {
        updateViewModelFromUrlView()
            .subscribe {
                finish()
            }
            .attachTo(destroyer)
    }

    class IntentBuilder(context: Context) : BaseIntentBuilder(context, BasicRequestSettingsActivity::class.java)

    companion object {

        private val METHODS = listOf(
            Shortcut.METHOD_GET,
            Shortcut.METHOD_POST,
            Shortcut.METHOD_PUT,
            Shortcut.METHOD_DELETE,
            Shortcut.METHOD_PATCH,
            Shortcut.METHOD_HEAD,
            Shortcut.METHOD_OPTIONS,
            Shortcut.METHOD_TRACE
        )

    }

}
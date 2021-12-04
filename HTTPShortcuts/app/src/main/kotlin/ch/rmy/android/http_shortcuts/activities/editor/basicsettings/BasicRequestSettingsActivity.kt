package ch.rmy.android.http_shortcuts.activities.editor.basicsettings

import android.content.Context
import android.os.Bundle
import android.widget.EditText
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.data.enums.ShortcutExecutionType
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.databinding.ActivityBasicRequestSettingsBinding
import ch.rmy.android.http_shortcuts.extensions.attachTo
import ch.rmy.android.http_shortcuts.extensions.bindViewModel
import ch.rmy.android.http_shortcuts.extensions.observeTextChanges
import ch.rmy.android.http_shortcuts.extensions.type
import ch.rmy.android.http_shortcuts.extensions.visible
import ch.rmy.android.http_shortcuts.utils.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
import ch.rmy.android.http_shortcuts.variables.VariableViewUtils.bindVariableViews
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
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

    private lateinit var binding: ActivityBasicRequestSettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = applyBinding(ActivityBasicRequestSettingsBinding.inflate(layoutInflater))
        setTitle(R.string.section_basic_request)

        initViews()
        bindViewsToViewModel()
    }

    private fun initViews() {
        binding.inputMethod.setItemsFromPairs(
            METHODS.map {
                it to it
            }
        )
        bindVariableViews(binding.inputUrl, binding.variableButtonUrl, variablePlaceholderProvider)
            .attachTo(destroyer)
    }

    private fun bindViewsToViewModel() {
        shortcutData.observe(this) {
            updateShortcutViews()
        }
        variablesData.observe(this) {
            updateShortcutViews()
        }

        binding.inputMethod.selectionChanges
            .concatMapCompletable { method -> viewModel.setMethod(method) }
            .subscribe()
            .attachTo(destroyer)
        bindTextChangeListener(binding.inputUrl) { shortcutData.value?.url }
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
        viewModel.setUrl(binding.inputUrl.rawString)

    private fun updateShortcutViews() {
        val shortcut = shortcutData.value ?: return

        binding.inputMethod.visible = shortcut.type == ShortcutExecutionType.APP
        binding.inputMethod.selectedItem = shortcut.method
        binding.inputUrl.rawString = shortcut.url
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
            Shortcut.METHOD_TRACE,
        )
    }
}

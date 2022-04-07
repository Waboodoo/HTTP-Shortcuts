package ch.rmy.android.http_shortcuts.activities.editor.basicsettings

import android.os.Bundle
import android.widget.ArrayAdapter
import ch.rmy.android.framework.extensions.attachTo
import ch.rmy.android.framework.extensions.bindViewModel
import ch.rmy.android.framework.extensions.initialize
import ch.rmy.android.framework.extensions.observe
import ch.rmy.android.framework.extensions.observeTextChanges
import ch.rmy.android.framework.extensions.setTextSafely
import ch.rmy.android.framework.extensions.visible
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.data.models.ShortcutModel
import ch.rmy.android.http_shortcuts.databinding.ActivityBasicRequestSettingsBinding
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
import ch.rmy.android.http_shortcuts.variables.VariableViewUtils.bindVariableViews

class BasicRequestSettingsActivity : BaseActivity() {

    private val viewModel: BasicRequestSettingsViewModel by bindViewModel()

    private val variablePlaceholderProvider = VariablePlaceholderProvider()

    private lateinit var binding: ActivityBasicRequestSettingsBinding

    private var suggestions: List<String> = emptyList()
        set(value) {
            if (field != value) {
                field = value
                binding.inputBrowserPackageName.setAdapter(ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, value))
            }
        }

    override fun onCreated(savedState: Bundle?) {
        viewModel.initialize()
        initViews()
        initUserInputBindings()
        initViewModelBindings()
    }

    private fun initViews() {
        binding = applyBinding(ActivityBasicRequestSettingsBinding.inflate(layoutInflater))
        setTitle(R.string.section_basic_request)

        binding.inputMethod.setItemsFromPairs(
            METHODS.map {
                it to it
            }
        )
        bindVariableViews(binding.inputUrl, binding.variableButtonUrl, variablePlaceholderProvider)
            .attachTo(destroyer)
    }

    private fun initUserInputBindings() {
        binding.inputMethod.selectionChanges
            .subscribe(viewModel::onMethodChanged)
            .attachTo(destroyer)

        binding.inputUrl
            .observeTextChanges()
            .subscribe {
                viewModel.onUrlChanged(binding.inputUrl.rawString)
            }
            .attachTo(destroyer)

        binding.inputBrowserPackageName
            .observeTextChanges()
            .subscribe {
                viewModel.onBrowserPackageNameChanged(it.toString())
            }
            .attachTo(destroyer)
    }

    private fun initViewModelBindings() {
        viewModel.viewState.observe(this) { viewState ->
            viewState.variables?.let(variablePlaceholderProvider::applyVariables)
            binding.inputMethod.visible = viewState.methodVisible
            binding.inputMethod.selectedItem = viewState.method
            binding.inputUrl.rawString = viewState.url
            binding.inputBrowserPackageName.setTextSafely(viewState.browserPackageName)
            binding.inputBrowserPackageName.visible = viewState.browserPackageNameVisible
            binding.labelBrowserPackageName.visible = viewState.browserPackageNameVisible
            suggestions = viewState.suggestedBrowserPackageNames
        }
        viewModel.events.observe(this, ::handleEvent)
    }

    override fun onBackPressed() {
        viewModel.onBackPressed()
    }

    class IntentBuilder : BaseIntentBuilder(BasicRequestSettingsActivity::class.java)

    companion object {

        private val METHODS = listOf(
            ShortcutModel.METHOD_GET,
            ShortcutModel.METHOD_POST,
            ShortcutModel.METHOD_PUT,
            ShortcutModel.METHOD_DELETE,
            ShortcutModel.METHOD_PATCH,
            ShortcutModel.METHOD_HEAD,
            ShortcutModel.METHOD_OPTIONS,
            ShortcutModel.METHOD_TRACE,
        )
    }
}

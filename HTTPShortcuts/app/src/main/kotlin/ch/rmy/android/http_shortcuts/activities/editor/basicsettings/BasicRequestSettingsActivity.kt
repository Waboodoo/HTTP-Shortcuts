package ch.rmy.android.http_shortcuts.activities.editor.basicsettings

import ch.rmy.android.framework.extensions.attachTo
import ch.rmy.android.framework.extensions.bindViewModel
import ch.rmy.android.framework.extensions.initialize
import ch.rmy.android.framework.extensions.observe
import ch.rmy.android.framework.extensions.observeTextChanges
import ch.rmy.android.framework.extensions.visible
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.databinding.ActivityBasicRequestSettingsBinding
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
import ch.rmy.android.http_shortcuts.variables.VariableViewUtils.bindVariableViews

class BasicRequestSettingsActivity : BaseActivity() {

    private val viewModel: BasicRequestSettingsViewModel by bindViewModel()

    private val variablePlaceholderProvider = VariablePlaceholderProvider()

    private lateinit var binding: ActivityBasicRequestSettingsBinding

    override fun onCreate() {
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
    }

    private fun initViewModelBindings() {
        viewModel.viewState.observe(this) { viewState ->
            binding.inputMethod.visible = viewState.methodVisible
            binding.inputMethod.selectedItem = viewState.method
            binding.inputUrl.rawString = viewState.url

            viewState.variables?.let(variablePlaceholderProvider::applyVariables)
        }
        viewModel.events.observe(this, ::handleEvent)
    }

    override fun onBackPressed() {
        viewModel.onBackPressed()
    }

    class IntentBuilder : BaseIntentBuilder(BasicRequestSettingsActivity::class.java)

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

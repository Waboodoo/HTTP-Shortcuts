package ch.rmy.android.http_shortcuts.activities.editor.response

import android.os.Bundle
import ch.rmy.android.framework.extensions.attachTo
import ch.rmy.android.framework.extensions.bindViewModel
import ch.rmy.android.framework.extensions.initialize
import ch.rmy.android.framework.extensions.observe
import ch.rmy.android.framework.extensions.observeChecked
import ch.rmy.android.framework.extensions.observeTextChanges
import ch.rmy.android.framework.extensions.setHint
import ch.rmy.android.framework.extensions.visible
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.data.models.ResponseHandling
import ch.rmy.android.http_shortcuts.databinding.ActivityResponseBinding
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
import ch.rmy.android.http_shortcuts.variables.VariableViewUtils

class ResponseActivity : BaseActivity() {

    private val viewModel: ResponseViewModel by bindViewModel()
    private val variablePlaceholderProvider = VariablePlaceholderProvider()

    private lateinit var binding: ActivityResponseBinding

    override fun onCreated(savedState: Bundle?) {
        viewModel.initialize()
        initViews()
        initUserInputBindings()
        initViewModelBindings()
    }

    private fun initViews() {
        binding = applyBinding(ActivityResponseBinding.inflate(layoutInflater))
        setTitle(R.string.label_response_handling)

        binding.inputResponseUiType.setItemsFromPairs(
            UI_TYPES.map {
                it.first to getString(it.second)
            }
        )
        binding.inputResponseSuccessOutput.setItemsFromPairs(
            SUCCESS_OUTPUT_TYPES.map {
                it.first to getString(it.second)
            }
        )
        binding.inputResponseFailureOutput.setItemsFromPairs(
            FAILURE_OUTPUT_TYPES.map {
                it.first to getString(it.second)
            }
        )

        binding.instructionsScriptingHint.text = getString(R.string.message_response_handling_scripting_hint, getString(R.string.label_scripting))
    }

    private fun initUserInputBindings() {
        binding.inputResponseUiType.selectionChanges
            .subscribe(viewModel::onResponseUiTypeChanged)
            .attachTo(destroyer)
        binding.inputResponseSuccessOutput
            .selectionChanges
            .subscribe(viewModel::onResponseSuccessOutputChanged)
            .attachTo(destroyer)
        binding.inputResponseFailureOutput
            .selectionChanges
            .subscribe(viewModel::onResponseFailureOutputChanged)
            .attachTo(destroyer)
        binding.inputSuccessMessage
            .observeTextChanges()
            .subscribe {
                viewModel.onSuccessMessageChanged(binding.inputSuccessMessage.rawString)
            }
            .attachTo(destroyer)
        binding.inputIncludeMetaInformation
            .observeChecked()
            .subscribe(viewModel::onIncludeMetaInformationChanged)
            .attachTo(destroyer)

        VariableViewUtils.bindVariableViews(binding.inputSuccessMessage, binding.variableButtonSuccessMessage, variablePlaceholderProvider)
    }

    private fun initViewModelBindings() {
        viewModel.viewState.observe(this) { viewState ->
            viewState.variables?.let(variablePlaceholderProvider::applyVariables)
            binding.inputSuccessMessage.setHint(viewState.successMessageHint)
            binding.inputResponseUiType.selectedItem = viewState.responseUiType
            binding.inputResponseSuccessOutput.selectedItem = viewState.responseSuccessOutput
            binding.inputResponseFailureOutput.selectedItem = viewState.responseFailureOutput
            binding.inputIncludeMetaInformation.isChecked = viewState.includeMetaInformation
            binding.inputSuccessMessage.rawString = viewState.successMessage
            binding.inputResponseUiType.visible = viewState.responseUiTypeVisible
            binding.containerInputSuccessMessage.visible = viewState.successMessageVisible
            binding.inputIncludeMetaInformation.visible = viewState.includeMetaInformationVisible
        }
        viewModel.events.observe(this, ::handleEvent)
    }

    override fun onBackPressed() {
        viewModel.onBackPressed()
    }

    class IntentBuilder : BaseIntentBuilder(ResponseActivity::class.java)

    companion object {

        private val UI_TYPES = listOf(
            ResponseHandling.UI_TYPE_TOAST to R.string.option_response_handling_type_toast,
            ResponseHandling.UI_TYPE_DIALOG to R.string.option_response_handling_type_dialog,
            ResponseHandling.UI_TYPE_WINDOW to R.string.option_response_handling_type_window,
        )

        private val SUCCESS_OUTPUT_TYPES = listOf(
            ResponseHandling.SUCCESS_OUTPUT_RESPONSE to R.string.option_response_handling_success_output_response,
            ResponseHandling.SUCCESS_OUTPUT_MESSAGE to R.string.option_response_handling_success_output_message,
            ResponseHandling.SUCCESS_OUTPUT_NONE to R.string.option_response_handling_success_output_none,
        )

        private val FAILURE_OUTPUT_TYPES = listOf(
            ResponseHandling.FAILURE_OUTPUT_DETAILED to R.string.option_response_handling_failure_output_detailed,
            ResponseHandling.FAILURE_OUTPUT_SIMPLE to R.string.option_response_handling_failure_output_simple,
            ResponseHandling.FAILURE_OUTPUT_NONE to R.string.option_response_handling_failure_output_none,
        )
    }
}

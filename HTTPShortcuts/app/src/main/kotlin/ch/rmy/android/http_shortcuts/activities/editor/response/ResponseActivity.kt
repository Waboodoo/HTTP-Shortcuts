package ch.rmy.android.http_shortcuts.activities.editor.response

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import ch.rmy.android.framework.extensions.bindViewModel
import ch.rmy.android.framework.extensions.collectEventsWhileActive
import ch.rmy.android.framework.extensions.collectViewStateWhileActive
import ch.rmy.android.framework.extensions.doOnCheckedChanged
import ch.rmy.android.framework.extensions.doOnTextChanged
import ch.rmy.android.framework.extensions.initialize
import ch.rmy.android.framework.extensions.isVisible
import ch.rmy.android.framework.extensions.setHint
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.framework.viewmodel.ViewModelEvent
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.dagger.ApplicationComponent
import ch.rmy.android.http_shortcuts.data.enums.ResponseDisplayAction
import ch.rmy.android.http_shortcuts.data.models.ResponseHandlingModel
import ch.rmy.android.http_shortcuts.databinding.ActivityResponseBinding
import kotlinx.coroutines.launch

class ResponseActivity : BaseActivity() {

    private val viewModel: ResponseViewModel by bindViewModel()

    private lateinit var binding: ActivityResponseBinding

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }

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
        binding.inputDialogAction.setItemsFromPairs(
            DIALOG_ACTIONS.map {
                it.first to getString(it.second)
            }
        )

        binding.instructionsScriptingHint.text = getString(R.string.message_response_handling_scripting_hint, getString(R.string.label_scripting))
        binding.inputShowShareButton.text = getString(R.string.label_response_actions_show_button, getString(R.string.share_button))
        binding.inputShowCopyButton.text = getString(R.string.label_response_actions_show_button, getString(R.string.action_copy_response))
        binding.inputShowRerunButton.text = getString(R.string.label_response_actions_show_button, getString(R.string.action_rerun_shortcut))
        binding.inputShowSaveButton.text = getString(R.string.label_response_actions_show_button, getString(R.string.button_save_response_as_file))
    }

    private fun initUserInputBindings() {
        lifecycleScope.launch {
            binding.inputResponseUiType.selectionChanges.collect(viewModel::onResponseUiTypeChanged)
        }
        lifecycleScope.launch {
            binding.inputResponseSuccessOutput.selectionChanges.collect(viewModel::onResponseSuccessOutputChanged)
        }
        lifecycleScope.launch {
            binding.inputResponseFailureOutput.selectionChanges.collect(viewModel::onResponseFailureOutputChanged)
        }
        binding.inputSuccessMessage.doOnTextChanged {
            viewModel.onSuccessMessageChanged(binding.inputSuccessMessage.rawString)
        }
        lifecycleScope.launch {
            binding.inputDialogAction.selectionChanges.collect { actionKey ->
                viewModel.onDialogActionChanged(
                    actionKey.takeUnless { it == DIALOG_ACTION_NONE }
                        ?.let(ResponseDisplayAction::parse)
                )
            }
        }
        binding.inputIncludeMetaInformation.doOnCheckedChanged(viewModel::onIncludeMetaInformationChanged)
        binding.inputShowSaveButton.doOnCheckedChanged { enabled ->
            viewModel.onShowActionButtonChanged(ResponseDisplayAction.SAVE, enabled)
        }
        binding.inputShowShareButton.doOnCheckedChanged { enabled ->
            viewModel.onShowActionButtonChanged(ResponseDisplayAction.SHARE, enabled)
        }
        binding.inputShowCopyButton.doOnCheckedChanged { enabled ->
            viewModel.onShowActionButtonChanged(ResponseDisplayAction.COPY, enabled)
        }
        binding.inputShowRerunButton.doOnCheckedChanged { enabled ->
            viewModel.onShowActionButtonChanged(ResponseDisplayAction.RERUN, enabled)
        }

        binding.variableButtonSuccessMessage.setOnClickListener {
            viewModel.onSuccessMessageVariableButtonClicked()
        }
    }

    private fun initViewModelBindings() {
        collectViewStateWhileActive(viewModel) { viewState ->
            binding.loadingIndicator.isVisible = false
            binding.inputSuccessMessage.setHint(viewState.successMessageHint)
            binding.inputResponseUiType.selectedItem = viewState.responseUiType
            binding.inputResponseSuccessOutput.selectedItem = viewState.responseSuccessOutput
            binding.inputResponseFailureOutput.selectedItem = viewState.responseFailureOutput
            binding.inputIncludeMetaInformation.isChecked = viewState.includeMetaInformation
            binding.inputSuccessMessage.rawString = viewState.successMessage
            binding.inputResponseUiType.isVisible = viewState.responseUiTypeVisible
            binding.containerInputSuccessMessage.isVisible = viewState.successMessageVisible
            binding.inputIncludeMetaInformation.isVisible = viewState.includeMetaInformationVisible
            binding.warningToastLimitations.isVisible = viewState.showToastInfo
            binding.inputDialogAction.isVisible = viewState.dialogActionVisible
            binding.inputDialogAction.selectedItem = viewState.dialogAction?.key ?: DIALOG_ACTION_NONE
            binding.inputShowCopyButton.isVisible = viewState.showActionButtonCheckboxes
            binding.inputShowSaveButton.isVisible = viewState.showActionButtonCheckboxes
            binding.inputShowRerunButton.isVisible = viewState.showActionButtonCheckboxes
            binding.inputShowShareButton.isVisible = viewState.showActionButtonCheckboxes
            binding.inputShowCopyButton.isChecked = viewState.showCopyActionEnabled
            binding.inputShowSaveButton.isChecked = viewState.showSaveActionEnabled
            binding.inputShowRerunButton.isChecked = viewState.showRerunActionEnabled
            binding.inputShowShareButton.isChecked = viewState.showShareActionEnabled
            binding.layoutContainer.isVisible = true
            setDialogState(viewState.dialogState, viewModel)
        }
        collectEventsWhileActive(viewModel, ::handleEvent)
    }

    override fun handleEvent(event: ViewModelEvent) {
        when (event) {
            is ResponseEvent.InsertVariablePlaceholder -> binding.inputSuccessMessage.insertVariablePlaceholder(event.variablePlaceholder)
            else -> super.handleEvent(event)
        }
    }

    override fun onBackPressed() {
        viewModel.onBackPressed()
    }

    class IntentBuilder : BaseIntentBuilder(ResponseActivity::class)

    companion object {

        private val UI_TYPES = listOf(
            ResponseHandlingModel.UI_TYPE_TOAST to R.string.option_response_handling_type_toast,
            ResponseHandlingModel.UI_TYPE_DIALOG to R.string.option_response_handling_type_dialog,
            ResponseHandlingModel.UI_TYPE_WINDOW to R.string.option_response_handling_type_window,
        )

        private val SUCCESS_OUTPUT_TYPES = listOf(
            ResponseHandlingModel.SUCCESS_OUTPUT_RESPONSE to R.string.option_response_handling_success_output_response,
            ResponseHandlingModel.SUCCESS_OUTPUT_MESSAGE to R.string.option_response_handling_success_output_message,
            ResponseHandlingModel.SUCCESS_OUTPUT_NONE to R.string.option_response_handling_success_output_none,
        )

        private val FAILURE_OUTPUT_TYPES = listOf(
            ResponseHandlingModel.FAILURE_OUTPUT_DETAILED to R.string.option_response_handling_failure_output_detailed,
            ResponseHandlingModel.FAILURE_OUTPUT_SIMPLE to R.string.option_response_handling_failure_output_simple,
            ResponseHandlingModel.FAILURE_OUTPUT_NONE to R.string.option_response_handling_failure_output_none,
        )

        private const val DIALOG_ACTION_NONE = "none"
        private val DIALOG_ACTIONS = listOf(
            DIALOG_ACTION_NONE to R.string.label_dialog_action_none,
            ResponseDisplayAction.RERUN.key to R.string.action_rerun_shortcut,
            ResponseDisplayAction.SHARE.key to R.string.share_button,
            ResponseDisplayAction.COPY.key to R.string.action_copy_response,
        )
    }
}

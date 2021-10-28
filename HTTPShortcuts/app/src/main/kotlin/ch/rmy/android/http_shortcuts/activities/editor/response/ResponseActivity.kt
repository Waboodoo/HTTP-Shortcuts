package ch.rmy.android.http_shortcuts.activities.editor.response

import android.content.Context
import android.os.Bundle
import android.widget.EditText
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.data.models.ResponseHandling
import ch.rmy.android.http_shortcuts.databinding.ActivityResponseBinding
import ch.rmy.android.http_shortcuts.extensions.attachTo
import ch.rmy.android.http_shortcuts.extensions.bindViewModel
import ch.rmy.android.http_shortcuts.extensions.observeTextChanges
import ch.rmy.android.http_shortcuts.extensions.toLiveData
import ch.rmy.android.http_shortcuts.extensions.visible
import ch.rmy.android.http_shortcuts.utils.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
import ch.rmy.android.http_shortcuts.variables.VariableViewUtils
import ch.rmy.android.http_shortcuts.views.LabelledSpinner
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

class ResponseActivity : BaseActivity() {

    private val viewModel: ResponseViewModel by bindViewModel()
    private val shortcutData by lazy {
        viewModel.shortcut
    }
    private val variablesData by lazy {
        viewModel.variables
    }
    private val variablePlaceholderProvider by lazy {
        VariablePlaceholderProvider(variablesData)
    }

    private var responseHandlingBound = false
    private var successMessageInitialized = false

    private lateinit var binding: ActivityResponseBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = applyBinding(ActivityResponseBinding.inflate(layoutInflater))
        setTitle(R.string.label_response_handling)

        initViews()
        bindViewsToViewModel()
    }

    private fun initViews() {
        binding.inputResponseUiType.setItemsFromPairs(UI_TYPES.map {
            it.first to getString(it.second)
        })
        binding.inputResponseSuccessOutput.setItemsFromPairs(SUCCESS_OUTPUT_TYPES.map {
            it.first to getString(it.second)
        })
        binding.inputResponseFailureOutput.setItemsFromPairs(FAILURE_OUTPUT_TYPES.map {
            it.first to getString(it.second)
        })

        VariableViewUtils.bindVariableViews(binding.inputSuccessMessage, binding.variableButtonSuccessMessage, variablePlaceholderProvider)

        binding.instructionsScriptingHint.text = getString(R.string.message_response_handling_scripting_hint, getString(R.string.label_scripting))
    }

    private fun bindViewsToViewModel() {
        shortcutData.observe(this) {
            updateShortcutViews()
            if (!responseHandlingBound && shortcutData.value?.responseHandling != null) {
                shortcutData.value!!.responseHandling!!.toLiveData().observe(this) {
                    updateShortcutViews()
                }
                responseHandlingBound = true
            }
        }

        bindSpinner(binding.inputResponseUiType)
        bindSpinner(binding.inputResponseSuccessOutput)
        bindSpinner(binding.inputResponseFailureOutput)

        bindTextChangeListener(binding.inputSuccessMessage) { shortcutData.value?.bodyContent }

        binding.inputIncludeMetaInformation.setOnCheckedChangeListener { _, _ ->
            updateViewModelFromViews()
                .subscribe()
                .attachTo(destroyer)
        }
    }

    private fun bindSpinner(spinner: LabelledSpinner) {
        spinner.selectionChanges
            .concatMapCompletable { updateViewModelFromViews() }
            .subscribe()
            .attachTo(destroyer)
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
        viewModel.setResponseHandling(
            uiType = binding.inputResponseUiType.selectedItem,
            successOutput = binding.inputResponseSuccessOutput.selectedItem,
            failureOutput = binding.inputResponseFailureOutput.selectedItem,
            successMessage = binding.inputSuccessMessage.rawString,
            includeMetaInfo = binding.inputIncludeMetaInformation.isChecked,
        )

    private fun updateShortcutViews() {
        val shortcut = shortcutData.value ?: return
        val responseHandling = shortcut.responseHandling ?: return

        binding.inputSuccessMessage.hint =
            String.format(getString(R.string.executed), shortcut.name.ifEmpty { getString(R.string.shortcut_safe_name) })

        binding.inputResponseUiType.selectedItem = responseHandling.uiType
        binding.inputResponseSuccessOutput.selectedItem = responseHandling.successOutput
        binding.inputResponseFailureOutput.selectedItem = responseHandling.failureOutput
        binding.inputIncludeMetaInformation.isChecked = responseHandling.includeMetaInfo

        if (!successMessageInitialized) {
            binding.inputSuccessMessage.rawString = responseHandling.successMessage
            successMessageInitialized = true
        }

        val hasOutput = responseHandling.successOutput != ResponseHandling.SUCCESS_OUTPUT_NONE
            || responseHandling.failureOutput != ResponseHandling.FAILURE_OUTPUT_NONE
        binding.inputResponseUiType.visible = hasOutput
        binding.containerInputSuccessMessage.visible = responseHandling.successOutput == ResponseHandling.SUCCESS_OUTPUT_MESSAGE
        binding.inputIncludeMetaInformation.visible = responseHandling.uiType == ResponseHandling.UI_TYPE_WINDOW && hasOutput
    }

    class IntentBuilder(context: Context) : BaseIntentBuilder(context, ResponseActivity::class.java)

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
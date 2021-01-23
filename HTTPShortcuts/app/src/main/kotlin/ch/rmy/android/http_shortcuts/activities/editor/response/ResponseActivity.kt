package ch.rmy.android.http_shortcuts.activities.editor.response

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.data.models.ResponseHandling
import ch.rmy.android.http_shortcuts.extensions.attachTo
import ch.rmy.android.http_shortcuts.extensions.bindViewModel
import ch.rmy.android.http_shortcuts.extensions.observeTextChanges
import ch.rmy.android.http_shortcuts.extensions.toLiveData
import ch.rmy.android.http_shortcuts.extensions.visible
import ch.rmy.android.http_shortcuts.utils.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.variables.VariableButton
import ch.rmy.android.http_shortcuts.variables.VariableEditText
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
import ch.rmy.android.http_shortcuts.variables.VariableViewUtils
import ch.rmy.android.http_shortcuts.views.LabelledSpinner
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import kotterknife.bindView
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

    private val responseTypeSpinner: LabelledSpinner by bindView(R.id.input_response_ui_type)
    private val successOutputSpinner: LabelledSpinner by bindView(R.id.input_response_success_output)
    private val failureOutputSpinner: LabelledSpinner by bindView(R.id.input_response_failure_output)
    private val includeMetaCheckbox: CheckBox by bindView(R.id.input_include_meta_information)
    private val successMessageContainer: View by bindView(R.id.container_input_success_message)
    private val successMessageView: VariableEditText by bindView(R.id.input_success_message)
    private val successMessageVariableButton: VariableButton by bindView(R.id.variable_button_success_message)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_response)
        setTitle(R.string.label_response_handling)

        initViews()
        bindViewsToViewModel()
    }

    private fun initViews() {
        responseTypeSpinner.setItemsFromPairs(UI_TYPES.map {
            it.first to getString(it.second)
        })
        successOutputSpinner.setItemsFromPairs(SUCCESS_OUTPUT_TYPES.map {
            it.first to getString(it.second)
        })
        failureOutputSpinner.setItemsFromPairs(FAILURE_OUTPUT_TYPES.map {
            it.first to getString(it.second)
        })

        VariableViewUtils.bindVariableViews(successMessageView, successMessageVariableButton, variablePlaceholderProvider)
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

        bindSpinner(responseTypeSpinner)
        bindSpinner(successOutputSpinner)
        bindSpinner(failureOutputSpinner)

        bindTextChangeListener(successMessageView) { shortcutData.value?.bodyContent }

        includeMetaCheckbox.setOnCheckedChangeListener { _, _ ->
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
            uiType = responseTypeSpinner.selectedItem,
            successOutput = successOutputSpinner.selectedItem,
            failureOutput = failureOutputSpinner.selectedItem,
            successMessage = successMessageView.rawString,
            includeMetaInfo = includeMetaCheckbox.isChecked
        )

    private fun updateShortcutViews() {
        val shortcut = shortcutData.value ?: return
        val responseHandling = shortcut.responseHandling ?: return

        successMessageView.hint = String.format(getString(R.string.executed), shortcut.name.ifEmpty { getString(R.string.shortcut_safe_name) })

        responseTypeSpinner.selectedItem = responseHandling.uiType
        successOutputSpinner.selectedItem = responseHandling.successOutput
        failureOutputSpinner.selectedItem = responseHandling.failureOutput
        includeMetaCheckbox.isChecked = responseHandling.includeMetaInfo

        if (!successMessageInitialized) {
            successMessageView.rawString = responseHandling.successMessage
            successMessageInitialized = true
        }

        val hasOutput = responseHandling.successOutput != ResponseHandling.SUCCESS_OUTPUT_NONE
            || responseHandling.failureOutput != ResponseHandling.FAILURE_OUTPUT_NONE
        responseTypeSpinner.visible = hasOutput
        successMessageContainer.visible = responseHandling.successOutput == ResponseHandling.SUCCESS_OUTPUT_MESSAGE
        includeMetaCheckbox.visible = responseHandling.uiType == ResponseHandling.UI_TYPE_WINDOW && hasOutput
    }

    class IntentBuilder(context: Context) : BaseIntentBuilder(context, ResponseActivity::class.java)

    companion object {

        private val UI_TYPES = listOf(
            ResponseHandling.UI_TYPE_TOAST to R.string.option_response_handling_type_toast,
            ResponseHandling.UI_TYPE_DIALOG to R.string.option_response_handling_type_dialog,
            ResponseHandling.UI_TYPE_WINDOW to R.string.option_response_handling_type_window
        )

        private val SUCCESS_OUTPUT_TYPES = listOf(
            ResponseHandling.SUCCESS_OUTPUT_RESPONSE to R.string.option_response_handling_success_output_response,
            ResponseHandling.SUCCESS_OUTPUT_MESSAGE to R.string.option_response_handling_success_output_message,
            ResponseHandling.SUCCESS_OUTPUT_NONE to R.string.option_response_handling_success_output_none
        )

        private val FAILURE_OUTPUT_TYPES = listOf(
            ResponseHandling.FAILURE_OUTPUT_DETAILED to R.string.option_response_handling_failure_output_detailed,
            ResponseHandling.FAILURE_OUTPUT_SIMPLE to R.string.option_response_handling_failure_output_simple,
            ResponseHandling.FAILURE_OUTPUT_NONE to R.string.option_response_handling_failure_output_none
        )

    }

}
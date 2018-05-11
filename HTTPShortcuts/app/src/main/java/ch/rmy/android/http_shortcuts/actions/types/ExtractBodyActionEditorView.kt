package ch.rmy.android.http_shortcuts.actions.types

import android.content.Context
import android.view.View
import android.widget.EditText
import android.widget.TextView
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.utils.ArrayUtil
import ch.rmy.android.http_shortcuts.utils.OnItemChosenListener
import ch.rmy.android.http_shortcuts.utils.fix
import ch.rmy.android.http_shortcuts.utils.visible
import ch.rmy.android.http_shortcuts.variables.VariableButton
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
import com.satsuware.usefulviews.LabelledSpinner
import kotterknife.bindView

class ExtractBodyActionEditorView(
        context: Context,
        private val action: ExtractBodyAction,
        variablePlaceholderProvider: VariablePlaceholderProvider
) : BaseActionEditorView(context, R.layout.action_editor_extract_body) {

    private val extractionOption: LabelledSpinner by bindView(R.id.input_extraction_option)
    private val substringOptions: View by bindView(R.id.container_substring_options)
    private val substringStart: EditText by bindView(R.id.input_substring_start_index)
    private val substringEnd: EditText by bindView(R.id.input_substring_end_index)
    private val jsonOptions: View by bindView(R.id.container_parse_json_options)
    private val jsonPath: EditText by bindView(R.id.input_json_path)

    private val targetVariableView: TextView by bindView(R.id.target_variable)
    private val variableButton: VariableButton by bindView(R.id.variable_button_target_variable)

    private var selectedVariableKey: String = action.variableKey

    init {
        extractionOption.fix()
        extractionOption.setItemsArray(getOptionStrings())
        extractionOption.setSelection(ArrayUtil.findIndex(EXTRACTION_OPTIONS, action.extractionType))
        extractionOption.onItemChosenListener = object : OnItemChosenListener() {
            override fun onSelectionChanged() {
                updateViews()
            }
        }

        substringStart.setText(action.substringStart.toString())
        substringEnd.setText(action.substringEnd.toString())

        jsonPath.setText(action.jsonPath)

        targetVariableView.text = action.variableKey
        targetVariableView.setOnClickListener {
            variableButton.performClick()
        }
        variableButton.variablePlaceholderProvider = variablePlaceholderProvider
        variableButton.variableSource.add {
            selectedVariableKey = it.variableKey
            updateViews()
        }.attachTo(destroyer)
        updateViews()
    }

    private fun updateViews() {
        if (selectedVariableKey.isEmpty()) {
            targetVariableView.setText(R.string.action_type_target_variable_no_variable_selected)
        } else {
            targetVariableView.text = selectedVariableKey
        }
        val selectedOption = getSelectedOption()
        substringOptions.visible = selectedOption == ExtractBodyAction.EXTRACTION_OPTION_SUBSTRING
        jsonOptions.visible = selectedOption == ExtractBodyAction.EXTRACTION_OPTION_PARSE_JSON
    }

    override fun compile(): Boolean {
        if (selectedVariableKey.isEmpty()) {
            return false
        }
        action.extractionType = getSelectedOption()
        action.variableKey = selectedVariableKey

        action.substringStart = substringStart.text.toString().toIntOrNull() ?: 0
        action.substringEnd = substringEnd.text.toString().toIntOrNull() ?: 0

        action.jsonPath = jsonPath.text.toString()

        return true
    }

    private fun getSelectedOption() =
            EXTRACTION_OPTIONS[extractionOption.spinner.selectedItemPosition]

    private fun getOptionStrings() = EXTRACTION_OPTIONS_RESOURCES.map { context.getString(it) }

    companion object {

        private val EXTRACTION_OPTIONS = arrayOf(
                ExtractBodyAction.EXTRACTION_OPTION_FULL_BODY,
                ExtractBodyAction.EXTRACTION_OPTION_SUBSTRING,
                ExtractBodyAction.EXTRACTION_OPTION_PARSE_JSON
        )

        private val EXTRACTION_OPTIONS_RESOURCES = intArrayOf(
                R.string.action_type_extract_body_description_option_full_body,
                R.string.action_type_extract_body_description_option_substring,
                R.string.action_type_extract_body_description_option_json
        )

    }

}
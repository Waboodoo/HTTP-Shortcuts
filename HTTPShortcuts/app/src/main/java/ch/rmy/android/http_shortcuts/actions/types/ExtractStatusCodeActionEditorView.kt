package ch.rmy.android.http_shortcuts.actions.types

import android.content.Context
import android.widget.TextView
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.variables.VariableButton
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
import kotterknife.bindView

class ExtractStatusCodeActionEditorView(
        context: Context,
        private val action: ExtractStatusCodeAction,
        variablePlaceholderProvider: VariablePlaceholderProvider
) : BaseActionEditorView(context, R.layout.action_editor_extract_status_code) {

    private val targetVariableView: TextView by bindView(R.id.target_variable)
    private val variableButton: VariableButton by bindView(R.id.variable_button_target_variable)

    private var selectedVariableKey: String = action.variableKey

    init {
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
    }

    override fun compile(): Boolean {
        if (selectedVariableKey.isEmpty()) {
            return false
        }
        action.variableKey = selectedVariableKey
        return true
    }

}
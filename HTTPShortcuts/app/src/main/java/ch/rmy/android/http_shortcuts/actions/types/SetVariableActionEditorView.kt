package ch.rmy.android.http_shortcuts.actions.types

import android.content.Context
import android.widget.TextView
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.utils.focus
import ch.rmy.android.http_shortcuts.variables.VariableButton
import ch.rmy.android.http_shortcuts.variables.VariableEditText
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
import kotterknife.bindView

class SetVariableActionEditorView(
        context: Context,
        private val action: SetVariableAction,
        variablePlaceholderProvider: VariablePlaceholderProvider
) : BaseActionEditorView(context, R.layout.action_editor_set_variable) {

    private val newValueView: VariableEditText by bindView(R.id.input_new_value)
    private val targetVariableView: TextView by bindView(R.id.target_variable)
    private val variableButton: VariableButton by bindView(R.id.variable_button_new_value)
    private val variableButton2: VariableButton by bindView(R.id.variable_button_target_variable)

    private var selectedVariableKey: String = action.variableKey

    init {
        newValueView.bind(variableButton, variablePlaceholderProvider)
        newValueView.rawString = action.newValue
        newValueView.focus()

        targetVariableView.text = action.variableKey
        targetVariableView.setOnClickListener {
            variableButton2.performClick()
        }
        variableButton2.variablePlaceholderProvider = variablePlaceholderProvider
        variableButton2.variableSource.add {
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
        action.newValue = newValueView.rawString
        action.variableKey = selectedVariableKey
        return true
    }

}
package ch.rmy.android.http_shortcuts.actions.types

import android.content.Context
import android.widget.TextView
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.extensions.attachTo
import ch.rmy.android.http_shortcuts.extensions.focus
import ch.rmy.android.http_shortcuts.variables.VariableButton
import ch.rmy.android.http_shortcuts.variables.VariableEditText
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider
import ch.rmy.android.http_shortcuts.variables.VariableViewUtils.bindVariableViews
import kotterknife.bindView

class SetVariableActionEditorView(
    context: Context,
    private val action: SetVariableAction,
    private val variablePlaceholderProvider: VariablePlaceholderProvider
) : BaseActionEditorView(context, R.layout.action_editor_set_variable) {

    private val newValueView: VariableEditText by bindView(R.id.input_new_value)
    private val targetVariableView: TextView by bindView(R.id.target_variable)
    private val variableButton: VariableButton by bindView(R.id.variable_button_new_value)
    private val variableButton2: VariableButton by bindView(R.id.variable_button_target_variable)

    private var selectedVariableId: String = action.variableId
        set(value) {
            field = value
            updateViews()
        }

    init {
        newValueView.variablePlaceholderProvider = variablePlaceholderProvider
        variableButton.variablePlaceholderProvider = variablePlaceholderProvider
        variableButton2.variablePlaceholderProvider = variablePlaceholderProvider

        newValueView.rawString = action.newValue
        newValueView.focus()

        targetVariableView.text = action.variableId
        targetVariableView.setOnClickListener {
            variableButton2.performClick()
        }
        bindVariableViews(newValueView, variableButton, variablePlaceholderProvider)
            .attachTo(destroyer)
        variableButton2.variablePlaceholderProvider = variablePlaceholderProvider
        variableButton2.variableSource
            .subscribe {
                selectedVariableId = it.variableId
            }
            .attachTo(destroyer)
        updateViews()
    }

    private fun updateViews() {
        val variablePlaceholder = variablePlaceholderProvider.findPlaceholderById(selectedVariableId)
        if (variablePlaceholder == null) {
            targetVariableView.setText(R.string.action_type_target_variable_no_variable_selected)
        } else {
            targetVariableView.text = selectedVariableId
        }
    }

    override fun compile(): Boolean {
        if (selectedVariableId.isEmpty()) {
            return false
        }
        action.newValue = newValueView.rawString
        action.variableId = selectedVariableId
        return true
    }

}
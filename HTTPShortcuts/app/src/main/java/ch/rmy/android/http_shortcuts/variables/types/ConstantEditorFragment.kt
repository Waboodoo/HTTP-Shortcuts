package ch.rmy.android.http_shortcuts.variables.types

import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.realm.models.Variable
import kotlinx.android.synthetic.main.variable_editor_constant.*

class ConstantEditorFragment : VariableEditorFragment() {

    override val layoutResource = R.layout.variable_editor_constant

    override fun updateViews(variable: Variable) {
        input_variable_value.setText(variable.value)
    }

    override fun compileIntoVariable(variable: Variable) {
        variable.value = input_variable_value.text.toString()
    }
}

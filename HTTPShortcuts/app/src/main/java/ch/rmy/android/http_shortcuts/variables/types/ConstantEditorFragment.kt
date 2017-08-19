package ch.rmy.android.http_shortcuts.variables.types

import android.widget.EditText
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.realm.models.Variable
import kotterknife.bindView

class ConstantEditorFragment : VariableEditorFragment() {

    override val layoutResource = R.layout.variable_editor_constant

    val inputVariableValue: EditText by bindView(R.id.input_variable_value)

    override fun updateViews(variable: Variable) {
        inputVariableValue.setText(variable.value)
    }

    override fun compileIntoVariable(variable: Variable) {
        variable.value = inputVariableValue.text.toString()
    }
}

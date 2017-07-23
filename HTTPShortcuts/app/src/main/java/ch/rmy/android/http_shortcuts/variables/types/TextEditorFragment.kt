package ch.rmy.android.http_shortcuts.variables.types

import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.realm.models.Variable
import kotlinx.android.synthetic.main.variable_editor_text.*

class TextEditorFragment : VariableEditorFragment() {

    override val layoutResource = R.layout.variable_editor_text

    override fun updateViews(variable: Variable) {
        input_remember_value.isChecked = variable.rememberValue
    }

    override fun compileIntoVariable(variable: Variable) {
        variable.rememberValue = input_remember_value.isChecked
    }
}

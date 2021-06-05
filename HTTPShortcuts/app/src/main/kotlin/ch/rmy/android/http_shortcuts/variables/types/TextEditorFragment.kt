package ch.rmy.android.http_shortcuts.variables.types

import android.widget.CheckBox
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.data.models.Variable
import kotterknife.bindView

class TextEditorFragment : VariableEditorFragment() {

    override val layoutResource = R.layout.variable_editor_text

    private val inputRememberValue: CheckBox by bindView(R.id.input_remember_value)
    private val multiline: CheckBox by bindView(R.id.input_multiline)

    override fun updateViews(variable: Variable) {
        inputRememberValue.isChecked = variable.rememberValue
        multiline.isChecked = variable.isMultiline
    }

    override fun compileIntoVariable(variable: Variable) {
        variable.rememberValue = inputRememberValue.isChecked
        variable.isMultiline = multiline.isChecked
    }
}

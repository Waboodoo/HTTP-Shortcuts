package ch.rmy.android.http_shortcuts.variables.types

import android.widget.CheckBox
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.data.models.Variable
import kotterknife.bindView

class ColorEditorFragment : VariableEditorFragment() {

    private lateinit var variable: Variable

    override val layoutResource = R.layout.variable_editor_color

    private val inputRememberValue: CheckBox by bindView(R.id.input_remember_value)

    override fun updateViews(variable: Variable) {
        this.variable = variable
        inputRememberValue.isChecked = variable.rememberValue
    }

    override fun validate() = true

    override fun compileIntoVariable(variable: Variable) {
        variable.rememberValue = inputRememberValue.isChecked
    }

}

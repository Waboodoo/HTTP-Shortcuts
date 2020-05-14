package ch.rmy.android.http_shortcuts.variables.types

import android.widget.CheckBox
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.views.LabelledSpinner
import kotterknife.bindView

class ColorEditorFragment : VariableEditorFragment() {

    private lateinit var variable: Variable

    override val layoutResource = R.layout.variable_editor_color

    private val inputRememberValue: CheckBox by bindView(R.id.input_remember_value)
    private val colorType: LabelledSpinner by bindView(R.id.input_variable_color_input_type)

    override fun updateViews(variable: Variable) {
        this.variable = variable
        inputRememberValue.isChecked = variable.rememberValue

        colorType.setItemsFromPairs(listOf(
            ColorType.TYPE_RGB to ColorType.TYPE_RGB,
            ColorType.TYPE_HSV to ColorType.TYPE_HSV
        ))
        colorType.selectedItem = variable.dataForType[ColorType.KEY_INPUT_TYPE] ?: ColorType.TYPE_RGB
    }

    override fun validate() = true

    override fun compileIntoVariable(variable: Variable) {
        variable.rememberValue = inputRememberValue.isChecked
        variable.dataForType = mapOf(ColorType.KEY_INPUT_TYPE to colorType.selectedItem)
    }

}

package ch.rmy.android.http_shortcuts.variables.types

import android.widget.CheckBox
import android.widget.EditText
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.realm.models.Variable
import ch.rmy.android.http_shortcuts.utils.showMessageDialog
import kotterknife.bindView

class SliderEditorFragment : VariableEditorFragment() {

    private var variable: Variable? = null

    override val layoutResource = R.layout.variable_editor_slider

    private val inputRememberValue: CheckBox by bindView(R.id.input_remember_value)
    private val inputMin: EditText by bindView(R.id.input_slider_min)
    private val inputMax: EditText by bindView(R.id.input_slider_max)
    private val inputStep: EditText by bindView(R.id.input_slider_step)

    override fun updateViews(variable: Variable) {
        this.variable = variable
        inputRememberValue.isChecked = variable.rememberValue

        inputMin.setText(SliderType.findMin(variable).toString())
        inputMax.setText(SliderType.findMax(variable).toString())
        inputStep.setText(SliderType.findStep(variable).toString())
    }

    override fun validate() =
            if (maxValue <= minValue) {
                showMessageDialog(R.string.error_slider_max_not_greater_than_min)
                false
            } else {
                true
            }

    override fun compileIntoVariable(variable: Variable) {
        variable.rememberValue = inputRememberValue.isChecked
        variable.dataForType = mapOf(SliderType.KEY_MAX to maxValue.toString(), SliderType.KEY_MIN to minValue.toString(), SliderType.KEY_STEP to stepValue.toString())
    }

    private val minValue
        get() = inputMin.text.toString().toIntOrNull() ?: SliderType.DEFAULT_MIN

    private val maxValue
        get() = inputMax.text.toString().toIntOrNull() ?: SliderType.DEFAULT_MAX

    private val stepValue
        get() = inputStep.text.toString().toIntOrNull() ?: SliderType.DEFAULT_STEP

}

package ch.rmy.android.http_shortcuts.variables.types

import android.view.LayoutInflater
import android.view.ViewGroup
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.databinding.VariableEditorSliderBinding
import ch.rmy.android.http_shortcuts.extensions.showMessageDialog

class SliderEditorFragment : VariableEditorFragment<VariableEditorSliderBinding>() {

    private var variable: Variable? = null

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?) =
        VariableEditorSliderBinding.inflate(inflater, container, false)

    override fun updateViews(variable: Variable) {
        this.variable = variable
        binding.inputRememberValue.isChecked = variable.rememberValue

        binding.inputSliderMin.setText(SliderType.findMin(variable).toString())
        binding.inputSliderMax.setText(SliderType.findMax(variable).toString())
        binding.inputSliderStep.setText(SliderType.findStep(variable).toString())
    }

    override fun validate() =
        when {
            maxValue <= minValue -> {
                showMessageDialog(R.string.error_slider_max_not_greater_than_min)
                false
            }
            stepSize <= 0 -> {
                showMessageDialog(R.string.error_slider_step_size_must_be_positive)
                false
            }
            else -> true
        }

    override fun compileIntoVariable(variable: Variable) {
        variable.rememberValue = binding.inputRememberValue.isChecked
        variable.dataForType = SliderType.getData(maxValue, minValue, stepSize)
    }

    private val minValue
        get() = binding.inputSliderMin.text.toString().toIntOrNull() ?: SliderType.DEFAULT_MIN

    private val maxValue
        get() = binding.inputSliderMax.text.toString().toIntOrNull() ?: SliderType.DEFAULT_MAX

    private val stepSize
        get() = binding.inputSliderStep.text.toString().toIntOrNull() ?: SliderType.DEFAULT_STEP
}

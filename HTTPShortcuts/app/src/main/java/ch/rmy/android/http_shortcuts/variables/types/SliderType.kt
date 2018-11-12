package ch.rmy.android.http_shortcuts.variables.types

import android.content.Context
import android.view.LayoutInflater
import android.widget.SeekBar
import android.widget.TextView
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.realm.Controller
import ch.rmy.android.http_shortcuts.realm.models.Variable
import ch.rmy.android.http_shortcuts.utils.SimpleOnSeekBarChangeListener
import ch.rmy.android.http_shortcuts.utils.showIfPossible
import org.jdeferred2.Deferred


internal class SliderType : BaseVariableType(), AsyncVariableType {

    override val hasTitle = true

    override fun createDialog(context: Context, controller: Controller, variable: Variable, deferredValue: Deferred<String, Unit, Unit>): () -> Unit {
        val view = LayoutInflater.from(context).inflate(R.layout.variable_dialog_slider, null)

        val slider = view.findViewById<SeekBar>(R.id.slider)
        val label = view.findViewById<TextView>(R.id.slider_value)

        slider.max = findSliderMax(variable)

        if (variable.rememberValue) {
            val value = variable.value?.toIntOrNull() ?: 0
            val sliderValue = (value - findMin(variable)) / findStep(variable)
            if (sliderValue >= 0 && sliderValue <= slider.max) {
                slider.progress = sliderValue
            }
        }

        slider.setOnSeekBarChangeListener(object : SimpleOnSeekBarChangeListener() {
            override fun onProgressChanged(slider: SeekBar, progress: Int, fromUser: Boolean) {
                label.text = findValue(slider, variable)
            }
        })
        label.text = findValue(slider, variable)

        val builder = BaseVariableType.createDialogBuilder(context, variable, deferredValue)
                .toDialogBuilder()
                .customView(view, true)
                .positiveText(R.string.dialog_ok)
                .negativeText(R.string.dialog_cancel)
                .onPositive { _, _ ->
                    if (variable.isValid) {
                        val value = findValue(slider, variable)
                        deferredValue.resolve(value)
                        if (variable.rememberValue) {
                            controller.setVariableValue(variable.id, value).subscribe()
                        }
                    }
                }
        return {
            builder.showIfPossible()
        }
    }

    private fun findSliderMax(variable: Variable): Int =
            ((findMax(variable) - findMin(variable)) / findStep(variable))

    private fun findValue(slider: SeekBar, variable: Variable): String =
            (slider.progress * findStep(variable) + findMin(variable)).toString()

    override fun createEditorFragment() = SliderEditorFragment()

    companion object {

        const val KEY_MIN = "min"
        const val KEY_MAX = "max"
        const val KEY_STEP = "step"

        val DEFAULT_MIN = 0
        val DEFAULT_MAX = 100
        val DEFAULT_STEP = 1

        fun findMax(variable: Variable): Int = variable.dataForType[KEY_MAX]?.toDoubleOrNull()?.toInt() ?: DEFAULT_MAX
        fun findMin(variable: Variable): Int = variable.dataForType[KEY_MIN]?.toDoubleOrNull()?.toInt() ?: DEFAULT_MIN
        fun findStep(variable: Variable): Int = variable.dataForType[KEY_STEP]?.toDoubleOrNull()?.toInt() ?: DEFAULT_STEP

    }

}

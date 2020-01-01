package ch.rmy.android.http_shortcuts.variables.types

import android.content.Context
import android.view.LayoutInflater
import android.widget.SeekBar
import android.widget.TextView
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.data.Commons
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.extensions.mapIf
import ch.rmy.android.http_shortcuts.utils.SimpleOnSeekBarChangeListener
import io.reactivex.Single


internal class SliderType : BaseVariableType(), AsyncVariableType {

    override val hasTitle = true

    override fun resolveValue(context: Context, variable: Variable): Single<String> =
        Single.create<String> { emitter ->
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

            createDialogBuilder(context, variable, emitter)
                .view(view)
                .positive(R.string.dialog_ok) {
                    if (variable.isValid) {
                        val value = findValue(slider, variable)
                        emitter.onSuccess(value)
                    }
                }
                .negative(R.string.dialog_cancel)
                .showIfPossible()
        }
            .mapIf(variable.rememberValue) {
                it.flatMap { resolvedValue ->
                    Commons.setVariableValue(variable.id, resolvedValue)
                        .toSingle { resolvedValue }
                }
            }

    private fun findSliderMax(variable: Variable): Int =
        ((findMax(variable) - findMin(variable)) / findStep(variable))

    private fun findValue(slider: SeekBar, variable: Variable): String =
        (slider.progress * findStep(variable) + findMin(variable)).toString()

    override fun createEditorFragment() = SliderEditorFragment()

    companion object {

        private const val KEY_MIN = "min"
        private const val KEY_MAX = "max"
        private const val KEY_STEP = "step"

        const val DEFAULT_MIN = 0
        const val DEFAULT_MAX = 100
        const val DEFAULT_STEP = 1

        fun findMax(variable: Variable): Int = variable.dataForType[KEY_MAX]?.toDoubleOrNull()?.toInt() ?: DEFAULT_MAX
        fun findMin(variable: Variable): Int = variable.dataForType[KEY_MIN]?.toDoubleOrNull()?.toInt() ?: DEFAULT_MIN
        fun findStep(variable: Variable): Int = variable.dataForType[KEY_STEP]?.toDoubleOrNull()?.toInt() ?: DEFAULT_STEP

        fun getData(maxValue: Int, minValue: Int, stepValue: Int) = mapOf(
            KEY_MAX to maxValue.toString(),
            KEY_MIN to minValue.toString(),
            KEY_STEP to stepValue.toString()
        )

    }

}

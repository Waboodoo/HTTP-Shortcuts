package ch.rmy.android.http_shortcuts.variables.types

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.widget.SeekBar
import ch.rmy.android.framework.extensions.runIf
import ch.rmy.android.framework.utils.SimpleOnSeekBarChangeListener
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.execute.DialogHandle
import ch.rmy.android.http_shortcuts.dagger.ApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableRepository
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.databinding.VariableDialogSliderBinding
import ch.rmy.android.http_shortcuts.extensions.showOrElse
import ch.rmy.android.http_shortcuts.utils.ActivityProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.math.roundToInt

class SliderType : BaseVariableType() {

    @Inject
    lateinit var variablesRepository: VariableRepository

    @Inject
    lateinit var activityProvider: ActivityProvider

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }

    @SuppressLint("SetTextI18n")
    override suspend fun resolveValue(variable: Variable, dialogHandle: DialogHandle): String {
        val value = withContext(Dispatchers.Main) {
            val activity = activityProvider.getActivity()
            suspendCancellableCoroutine<String> { continuation ->
                val range = findRange(variable)
                val prefix = findPrefix(variable)
                val suffix = findSuffix(variable)
                val binding = VariableDialogSliderBinding.inflate(LayoutInflater.from(activity))
                binding.slider.max = findSliderMax(range)

                if (variable.rememberValue) {
                    val value = variable.value?.toDoubleOrNull() ?: 0.0
                    val sliderValue = findSliderValue(value, range)
                    if (sliderValue >= 0 && sliderValue <= binding.slider.max) {
                        binding.slider.progress = sliderValue
                    }
                }

                binding.slider.setOnSeekBarChangeListener(object : SimpleOnSeekBarChangeListener {
                    override fun onProgressChanged(slider: SeekBar, progress: Int, fromUser: Boolean) {
                        binding.sliderValue.text = prefix + findValue(slider.progress, range) + suffix
                    }
                })
                binding.sliderValue.text = prefix + findValue(binding.slider.progress, range) + suffix

                createDialogBuilder(activity, variable, continuation)
                    .view(binding.root)
                    .positive(R.string.dialog_ok) {
                        val value = findValue(binding.slider.progress, range)
                        continuation.resume(value)
                    }
                    .negative(R.string.dialog_cancel)
                    .showOrElse {
                        continuation.cancel()
                    }
            }
        }
        if (variable.rememberValue) {
            variablesRepository.setVariableValue(variable.id, value)
        }
        return value
    }

    data class Range(
        val min: Double,
        val max: Double,
        val step: Double,
    ) {
        val isIntsOnly = min.toString().endsWith(".0") && max.toString().endsWith(".0") && step.toString().endsWith(".0")
    }

    companion object {

        private const val KEY_MIN = "min"
        private const val KEY_MAX = "max"
        private const val KEY_STEP = "step"
        private const val KEY_PREFIX = "prefix"
        private const val KEY_SUFFIX = "suffix"

        const val DEFAULT_MIN = 0.0
        const val DEFAULT_MAX = 100.0
        const val DEFAULT_STEP = 1.0

        fun findMax(variable: Variable): Double =
            variable.dataForType[KEY_MAX]?.toDoubleOrNull() ?: DEFAULT_MAX

        fun findMin(variable: Variable): Double =
            variable.dataForType[KEY_MIN]?.toDoubleOrNull() ?: DEFAULT_MIN

        fun findStep(variable: Variable): Double =
            variable.dataForType[KEY_STEP]?.toDoubleOrNull() ?: DEFAULT_STEP

        fun findPrefix(variable: Variable): String =
            variable.dataForType[KEY_PREFIX] ?: ""

        fun findSuffix(variable: Variable): String =
            variable.dataForType[KEY_SUFFIX] ?: ""

        fun getData(maxValue: Double, minValue: Double, stepValue: Double, prefix: String, suffix: String) = mapOf(
            KEY_MAX to maxValue.toString(),
            KEY_MIN to minValue.toString(),
            KEY_STEP to stepValue.toString(),
            KEY_PREFIX to prefix,
            KEY_SUFFIX to suffix,
        )

        internal fun findRange(variable: Variable): Range =
            Range(
                min = findMin(variable),
                max = findMax(variable),
                step = findStep(variable),
            )

        internal fun findSliderMax(range: Range): Int =
            with(range) {
                ((max - min) / step).toInt()
            }

        internal fun findSliderValue(value: Double, range: Range): Int =
            with(range) {
                ((value - min) / step).toInt()
            }

        internal fun findValue(sliderValue: Int, range: Range): String =
            with(range) {
                ((10000 * (sliderValue * step + min)).roundToInt() / 10000.0).toString()
                    .runIf(isIntsOnly) {
                        removeSuffix(".0")
                    }
            }
    }
}

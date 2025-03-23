package ch.rmy.android.http_shortcuts.variables.types

import android.annotation.SuppressLint
import ch.rmy.android.framework.extensions.runIf
import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.framework.extensions.toLocalizable
import ch.rmy.android.http_shortcuts.activities.execute.DialogHandle
import ch.rmy.android.http_shortcuts.activities.execute.ExecuteDialogState
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableRepository
import ch.rmy.android.http_shortcuts.data.models.Variable
import javax.inject.Inject

class SliderType
@Inject
constructor(
    private val variablesRepository: VariableRepository,
) : VariableType {
    @SuppressLint("SetTextI18n")
    override suspend fun resolve(variable: Variable, dialogHandle: DialogHandle): String {
        val value = dialogHandle.showDialog(
            ExecuteDialogState.NumberSlider(
                title = variable.title.takeUnlessEmpty()?.toLocalizable(),
                message = variable.message.takeUnlessEmpty()?.toLocalizable(),
                initialValue = variable.realValue?.takeIf { variable.rememberValue }?.toFloatOrNull(),
                min = variable.findMin(),
                max = variable.findMax(),
                stepSize = variable.findStep(),
                prefix = variable.findPrefix(),
                suffix = variable.findSuffix(),
            ),
        )
            .toString()
            .runIf(variable.isIntsOnly()) {
                removeSuffix(".0")
            }

        if (variable.rememberValue) {
            variablesRepository.setVariableValue(variable.id, value)
        }
        return value
    }

    companion object {

        private const val KEY_MIN = "min"
        private const val KEY_MAX = "max"
        private const val KEY_STEP = "step"
        private const val KEY_PREFIX = "prefix"
        private const val KEY_SUFFIX = "suffix"

        const val DEFAULT_MIN = 0.0f
        const val DEFAULT_MAX = 100.0f
        const val DEFAULT_STEP = 1.0f

        fun Variable.findMax(): Float =
            getStringData(KEY_MAX)?.toFloatOrNull() ?: DEFAULT_MAX

        fun Variable.findMin(): Float =
            getStringData(KEY_MIN)?.toFloatOrNull() ?: DEFAULT_MIN

        fun Variable.findStep(): Float =
            getStringData(KEY_STEP)?.toFloatOrNull() ?: DEFAULT_STEP

        fun Variable.isIntsOnly() =
            findMin().toString().endsWith(".0") && findMax().toString().endsWith(".0") && findStep().toString().endsWith(".0")

        fun Variable.findPrefix(): String =
            getStringData(KEY_PREFIX) ?: ""

        fun Variable.findSuffix(): String =
            getStringData(KEY_SUFFIX) ?: ""

        fun getData(maxValue: Float, minValue: Float, stepValue: Float, prefix: String, suffix: String) = mapOf(
            KEY_MAX to maxValue.toString(),
            KEY_MIN to minValue.toString(),
            KEY_STEP to stepValue.toString(),
            KEY_PREFIX to prefix,
            KEY_SUFFIX to suffix,
        )
    }
}

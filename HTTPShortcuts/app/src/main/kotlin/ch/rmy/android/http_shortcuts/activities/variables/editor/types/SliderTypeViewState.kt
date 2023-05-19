package ch.rmy.android.http_shortcuts.activities.variables.editor.types

import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.variables.types.SliderType

@Stable
data class SliderTypeViewState(
    val minValueText: String,
    val maxValueText: String,
    val stepSizeText: String,
    val prefix: String,
    val suffix: String,
    val rememberValue: Boolean,
) : VariableTypeViewState {
    val minValue
        get() = minValueText.toFloatOrNull() ?: SliderType.DEFAULT_MIN

    val maxValue
        get() = maxValueText.toFloatOrNull() ?: SliderType.DEFAULT_MAX

    val stepSize
        get() = stepSizeText.toFloatOrNull() ?: SliderType.DEFAULT_STEP

    val isMaxValueInvalid
        get() = maxValue <= minValue

    val isStepSizeInvalid
        get() = stepSize <= 0
}

package ch.rmy.android.http_shortcuts.activities.variables.editor.types.slider

import ch.rmy.android.http_shortcuts.variables.types.SliderType

data class SliderTypeViewState(
    val minValueText: String,
    val maxValueText: String,
    val stepSizeText: String,
    val rememberValue: Boolean,
) {
    val minValue
        get() = minValueText.toIntOrNull() ?: SliderType.DEFAULT_MIN

    val maxValue
        get() = maxValueText.toIntOrNull() ?: SliderType.DEFAULT_MAX

    val stepSize
        get() = stepSizeText.toIntOrNull() ?: SliderType.DEFAULT_STEP
}

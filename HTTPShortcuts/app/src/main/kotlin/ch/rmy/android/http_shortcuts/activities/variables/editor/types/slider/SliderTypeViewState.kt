package ch.rmy.android.http_shortcuts.activities.variables.editor.types.slider

import ch.rmy.android.http_shortcuts.variables.types.SliderType

data class SliderTypeViewState(
    val minValueText: String,
    val maxValueText: String,
    val stepSizeText: String,
    val prefix: String,
    val suffix: String,
    val rememberValue: Boolean,
) {
    val minValue
        get() = minValueText.toDoubleOrNull() ?: SliderType.DEFAULT_MIN

    val maxValue
        get() = maxValueText.toDoubleOrNull() ?: SliderType.DEFAULT_MAX

    val stepSize
        get() = stepSizeText.toDoubleOrNull() ?: SliderType.DEFAULT_STEP
}

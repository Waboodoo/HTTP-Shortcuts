package ch.rmy.android.http_shortcuts.activities.variables.editor.types.slider

import android.app.Application
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.variables.editor.types.BaseVariableTypeViewModel
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.variables.types.SliderType

class SliderTypeViewModel(application: Application) : BaseVariableTypeViewModel<Unit, SliderTypeViewState>(application) {

    init {
        getApplicationComponent().inject(this)
    }

    override fun initViewState() = SliderTypeViewState(
        rememberValue = variable.rememberValue,
        minValueText = SliderType.findMin(variable).toString(),
        maxValueText = SliderType.findMax(variable).toString(),
        stepSizeText = SliderType.findStep(variable).toString(),
    )

    fun onRememberValueChanged(enabled: Boolean) {
        performOperation(
            temporaryVariableRepository.setRememberValue(enabled)
        )
    }

    fun onMinValueChanged(minValue: String) {
        updateViewState {
            copy(minValueText = minValue)
        }
        storeData()
    }

    fun onMaxValueChanged(maxValue: String) {
        updateViewState {
            copy(maxValueText = maxValue)
        }
        storeData()
    }

    fun onStepSizeChanged(stepSize: String) {
        updateViewState {
            copy(stepSizeText = stepSize)
        }
        storeData()
    }

    private fun storeData() {
        doWithViewState { viewState ->
            performOperation(
                temporaryVariableRepository.setDataForType(
                    SliderType.getData(
                        maxValue = viewState.maxValue,
                        minValue = viewState.minValue,
                        stepValue = viewState.stepSize,
                    )
                )
            )
        }
    }

    override fun validate(): Boolean =
        with(currentViewState!!) {
            when {
                maxValue <= minValue -> {
                    showToast(R.string.error_slider_max_not_greater_than_min)
                    false
                }
                stepSize <= 0 -> {
                    showToast(R.string.error_slider_step_size_must_be_positive)
                    false
                }
                else -> true
            }
        }
}

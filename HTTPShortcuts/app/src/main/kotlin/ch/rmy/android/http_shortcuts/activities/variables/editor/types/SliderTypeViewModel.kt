package ch.rmy.android.http_shortcuts.activities.variables.editor.types

import ch.rmy.android.http_shortcuts.data.domains.variables.TemporaryVariableRepository
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.variables.types.SliderType

class SliderTypeViewModel : BaseTypeViewModel() {

    override fun createViewState(variable: Variable) = SliderTypeViewState(
        rememberValue = variable.rememberValue,
        minValueText = SliderType.findMin(variable).toString(),
        maxValueText = SliderType.findMax(variable).toString(),
        stepSizeText = SliderType.findStep(variable).toString(),
        prefix = SliderType.findPrefix(variable),
        suffix = SliderType.findSuffix(variable),
    )

    override suspend fun save(temporaryVariableRepository: TemporaryVariableRepository, viewState: VariableTypeViewState) {
        viewState as SliderTypeViewState
        temporaryVariableRepository.setRememberValue(viewState.rememberValue)
        temporaryVariableRepository.setDataForType(
            SliderType.getData(
                maxValue = viewState.maxValue,
                minValue = viewState.minValue,
                stepValue = viewState.stepSize,
                prefix = viewState.prefix,
                suffix = viewState.suffix,
            )
        )
    }

    override fun validate(viewState: VariableTypeViewState): VariableTypeViewState? {
        viewState as SliderTypeViewState
        if (viewState.isMaxValueInvalid || viewState.isStepSizeInvalid) {
            return viewState
        }
        return null
    }
}

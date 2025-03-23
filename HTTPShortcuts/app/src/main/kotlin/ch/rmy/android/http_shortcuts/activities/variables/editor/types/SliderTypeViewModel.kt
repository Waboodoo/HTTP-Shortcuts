package ch.rmy.android.http_shortcuts.activities.variables.editor.types

import ch.rmy.android.http_shortcuts.data.domains.variables.TemporaryVariableRepository
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.variables.types.SliderType
import ch.rmy.android.http_shortcuts.variables.types.SliderType.Companion.findMax
import ch.rmy.android.http_shortcuts.variables.types.SliderType.Companion.findMin
import ch.rmy.android.http_shortcuts.variables.types.SliderType.Companion.findPrefix
import ch.rmy.android.http_shortcuts.variables.types.SliderType.Companion.findStep
import ch.rmy.android.http_shortcuts.variables.types.SliderType.Companion.findSuffix

class SliderTypeViewModel : BaseTypeViewModel() {

    override fun createViewState(variable: Variable) = SliderTypeViewState(
        rememberValue = variable.rememberValue,
        minValueText = variable.findMin().toString(),
        maxValueText = variable.findMax().toString(),
        stepSizeText = variable.findStep().toString(),
        prefix = variable.findPrefix(),
        suffix = variable.findSuffix(),
    )

    override suspend fun save(temporaryVariableRepository: TemporaryVariableRepository, viewState: VariableTypeViewState) {
        viewState as SliderTypeViewState
        temporaryVariableRepository.setRememberValue(viewState.rememberValue)
        temporaryVariableRepository.setData(
            SliderType.getData(
                maxValue = viewState.maxValue,
                minValue = viewState.minValue,
                stepValue = viewState.stepSize,
                prefix = viewState.prefix,
                suffix = viewState.suffix,
            ),
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

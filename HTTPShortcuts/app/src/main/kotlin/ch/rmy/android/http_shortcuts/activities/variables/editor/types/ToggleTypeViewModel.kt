package ch.rmy.android.http_shortcuts.activities.variables.editor.types

import ch.rmy.android.http_shortcuts.data.domains.variables.TemporaryVariableRepository
import ch.rmy.android.http_shortcuts.data.models.Option
import ch.rmy.android.http_shortcuts.data.models.Variable

class ToggleTypeViewModel : BaseTypeViewModel() {

    override fun createViewState(variable: Variable) = ToggleTypeViewState(
        options = variable.options?.map { ToggleTypeViewState.OptionItem(it.id, it.value) } ?: emptyList(),
    )

    override suspend fun save(temporaryVariableRepository: TemporaryVariableRepository, viewState: VariableTypeViewState) {
        viewState as ToggleTypeViewState
        temporaryVariableRepository.setOptions(
            viewState.options.map {
                Option(id = it.id, value = it.text)
            }
        )
    }

    override fun validate(viewState: VariableTypeViewState): VariableTypeViewState? {
        viewState as ToggleTypeViewState
        if (viewState.options.size < 2) {
            return viewState.copy(
                tooFewOptionsError = true,
            )
        }
        return null
    }
}

package ch.rmy.android.http_shortcuts.activities.variables.editor.types

import ch.rmy.android.framework.utils.UUIDUtils.newUUID
import ch.rmy.android.http_shortcuts.data.domains.variables.TemporaryVariableRepository
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.variables.types.ToggleType

class ToggleTypeViewModel : BaseTypeViewModel() {

    override fun createViewState(variable: Variable) = ToggleTypeViewState(
        options = variable.getStringListData(ToggleType.KEY_VALUES)?.map { ToggleTypeViewState.OptionItem(newUUID(), it) } ?: emptyList(),
    )

    override suspend fun save(temporaryVariableRepository: TemporaryVariableRepository, viewState: VariableTypeViewState) {
        viewState as ToggleTypeViewState
        temporaryVariableRepository.setData(
            mapOf(
                ToggleType.KEY_VALUES to viewState.options.map { it.text },
            ),
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

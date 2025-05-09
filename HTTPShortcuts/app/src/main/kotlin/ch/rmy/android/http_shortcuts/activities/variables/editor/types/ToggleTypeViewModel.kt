package ch.rmy.android.http_shortcuts.activities.variables.editor.types

import ch.rmy.android.framework.utils.UUIDUtils.newUUID
import ch.rmy.android.http_shortcuts.data.domains.variables.TemporaryGlobalVariableRepository
import ch.rmy.android.http_shortcuts.data.models.GlobalVariable
import ch.rmy.android.http_shortcuts.variables.types.ToggleType

class ToggleTypeViewModel : BaseTypeViewModel() {

    override fun createViewState(variable: GlobalVariable) = ToggleTypeViewState(
        options = variable.getStringListData(ToggleType.KEY_VALUES)?.map { ToggleTypeViewState.OptionItem(newUUID(), it) } ?: emptyList(),
    )

    override suspend fun save(temporaryGlobalVariableRepository: TemporaryGlobalVariableRepository, viewState: VariableTypeViewState) {
        viewState as ToggleTypeViewState
        temporaryGlobalVariableRepository.setData(
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

package ch.rmy.android.http_shortcuts.activities.variables.editor.types

import ch.rmy.android.http_shortcuts.data.domains.variables.TemporaryVariableRepository
import ch.rmy.android.http_shortcuts.data.models.Option
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.variables.types.SelectType

class SelectTypeViewModel : BaseTypeViewModel() {

    override fun createViewState(variable: Variable) = SelectTypeViewState(
        options = variable.options?.map {
            SelectTypeViewState.OptionItem(
                id = it.id,
                label = it.label,
                text = it.value,
            )
        } ?: emptyList(),
        isMultiSelect = SelectType.isMultiSelect(variable),
        separator = SelectType.getSeparator(variable),
    )

    override suspend fun save(temporaryVariableRepository: TemporaryVariableRepository, viewState: VariableTypeViewState) {
        viewState as SelectTypeViewState
        temporaryVariableRepository.setOptions(
            viewState.options.map {
                Option(id = it.id, value = it.text, label = it.label)
            }
        )
        temporaryVariableRepository.setDataForType(
            mapOf(
                SelectType.KEY_MULTI_SELECT to viewState.isMultiSelect.toString(),
                SelectType.KEY_SEPARATOR to viewState.separator,
            )
        )
    }

    override fun validate(viewState: VariableTypeViewState): VariableTypeViewState? {
        viewState as SelectTypeViewState
        if (viewState.options.isEmpty()) {
            return viewState.copy(
                tooFewOptionsError = true,
            )
        }
        return null
    }
}

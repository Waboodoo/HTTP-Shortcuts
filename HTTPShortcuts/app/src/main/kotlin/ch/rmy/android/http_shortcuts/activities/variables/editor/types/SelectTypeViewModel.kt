package ch.rmy.android.http_shortcuts.activities.variables.editor.types

import ch.rmy.android.framework.utils.UUIDUtils.newUUID
import ch.rmy.android.http_shortcuts.data.domains.variables.TemporaryVariableRepository
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.variables.types.SelectType

class SelectTypeViewModel : BaseTypeViewModel() {

    override fun createViewState(variable: Variable) = SelectTypeViewState(
        options = run {
            val labels = variable.getStringListData(SelectType.KEY_LABELS) ?: emptyList<String>()
            val values = variable.getStringListData(SelectType.KEY_VALUES) ?: emptyList<String>()
            labels.zip(values)
        }.map { (label, value) ->
            SelectTypeViewState.OptionItem(
                id = newUUID(),
                label = label,
                text = value,
            )
        },
        isMultiSelect = SelectType.isMultiSelect(variable),
        separator = SelectType.getSeparator(variable),
    )

    override suspend fun save(temporaryVariableRepository: TemporaryVariableRepository, viewState: VariableTypeViewState) {
        viewState as SelectTypeViewState
        temporaryVariableRepository.setData(
            mapOf(
                SelectType.KEY_LABELS to viewState.options.map { it.label },
                SelectType.KEY_VALUES to viewState.options.map { it.text },
                SelectType.KEY_MULTI_SELECT to viewState.isMultiSelect.toString(),
                SelectType.KEY_SEPARATOR to viewState.separator,
            ),
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

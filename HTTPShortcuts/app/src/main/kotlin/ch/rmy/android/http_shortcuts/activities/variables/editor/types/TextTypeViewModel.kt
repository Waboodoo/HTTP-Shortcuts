package ch.rmy.android.http_shortcuts.activities.variables.editor.types

import ch.rmy.android.http_shortcuts.data.domains.variables.TemporaryVariableRepository
import ch.rmy.android.http_shortcuts.data.enums.VariableType
import ch.rmy.android.http_shortcuts.data.models.Variable

class TextTypeViewModel : BaseTypeViewModel() {

    override fun createViewState(variable: Variable) = TextTypeViewState(
        rememberValue = variable.rememberValue,
        isMultiline = variable.isMultiline,
        isMultilineCheckboxVisible = variable.type == VariableType.TEXT,
    )

    override suspend fun save(temporaryVariableRepository: TemporaryVariableRepository, viewState: VariableTypeViewState) {
        viewState as TextTypeViewState
        temporaryVariableRepository.setRememberValue(viewState.rememberValue)
        temporaryVariableRepository.setMultiline(viewState.isMultiline)
    }
}

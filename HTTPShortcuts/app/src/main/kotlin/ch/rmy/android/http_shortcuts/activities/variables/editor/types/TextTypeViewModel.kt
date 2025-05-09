package ch.rmy.android.http_shortcuts.activities.variables.editor.types

import ch.rmy.android.http_shortcuts.data.domains.variables.TemporaryGlobalVariableRepository
import ch.rmy.android.http_shortcuts.data.enums.VariableType
import ch.rmy.android.http_shortcuts.data.models.GlobalVariable

class TextTypeViewModel : BaseTypeViewModel() {

    override fun createViewState(variable: GlobalVariable) = TextTypeViewState(
        rememberValue = variable.rememberValue,
        isMultiline = variable.isMultiline,
        isMultilineCheckboxVisible = variable.type == VariableType.TEXT,
    )

    override suspend fun save(temporaryGlobalVariableRepository: TemporaryGlobalVariableRepository, viewState: VariableTypeViewState) {
        viewState as TextTypeViewState
        temporaryGlobalVariableRepository.setRememberValue(viewState.rememberValue)
        temporaryGlobalVariableRepository.setMultiline(viewState.isMultiline)
    }
}

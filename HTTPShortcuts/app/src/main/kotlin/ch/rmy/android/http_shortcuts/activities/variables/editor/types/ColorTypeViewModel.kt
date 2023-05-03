package ch.rmy.android.http_shortcuts.activities.variables.editor.types

import ch.rmy.android.http_shortcuts.data.domains.variables.TemporaryVariableRepository
import ch.rmy.android.http_shortcuts.data.models.Variable

class ColorTypeViewModel : BaseTypeViewModel() {
    override fun createViewState(variable: Variable) = ColorTypeViewState(
        rememberValue = variable.rememberValue,
    )

    override suspend fun save(temporaryVariableRepository: TemporaryVariableRepository, viewState: VariableTypeViewState) {
        viewState as ColorTypeViewState
        temporaryVariableRepository.setRememberValue(viewState.rememberValue)
    }
}

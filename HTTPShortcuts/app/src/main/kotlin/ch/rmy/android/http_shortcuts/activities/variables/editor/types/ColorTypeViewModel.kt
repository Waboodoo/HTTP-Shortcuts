package ch.rmy.android.http_shortcuts.activities.variables.editor.types

import ch.rmy.android.http_shortcuts.data.domains.variables.TemporaryGlobalVariableRepository
import ch.rmy.android.http_shortcuts.data.models.GlobalVariable

class ColorTypeViewModel : BaseTypeViewModel() {
    override fun createViewState(variable: GlobalVariable) = ColorTypeViewState(
        rememberValue = variable.rememberValue,
    )

    override suspend fun save(temporaryGlobalVariableRepository: TemporaryGlobalVariableRepository, viewState: VariableTypeViewState) {
        viewState as ColorTypeViewState
        temporaryGlobalVariableRepository.setRememberValue(viewState.rememberValue)
    }
}

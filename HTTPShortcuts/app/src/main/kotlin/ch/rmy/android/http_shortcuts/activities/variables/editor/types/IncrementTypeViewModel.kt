package ch.rmy.android.http_shortcuts.activities.variables.editor.types

import ch.rmy.android.http_shortcuts.data.domains.variables.TemporaryGlobalVariableRepository
import ch.rmy.android.http_shortcuts.data.models.GlobalVariable

class IncrementTypeViewModel : BaseTypeViewModel() {

    override fun createViewState(variable: GlobalVariable) = IncrementTypeViewState(
        value = (variable.value?.toLongOrNull() ?: 0).toString(),
    )

    override suspend fun save(temporaryGlobalVariableRepository: TemporaryGlobalVariableRepository, viewState: VariableTypeViewState) {
        viewState as IncrementTypeViewState
        temporaryGlobalVariableRepository.setValue(viewState.value)
    }
}

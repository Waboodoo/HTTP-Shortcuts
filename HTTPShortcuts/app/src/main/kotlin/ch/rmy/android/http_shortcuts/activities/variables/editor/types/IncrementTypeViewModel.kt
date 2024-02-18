package ch.rmy.android.http_shortcuts.activities.variables.editor.types

import ch.rmy.android.http_shortcuts.data.domains.variables.TemporaryVariableRepository
import ch.rmy.android.http_shortcuts.data.models.Variable

class IncrementTypeViewModel : BaseTypeViewModel() {

    override fun createViewState(variable: Variable) = IncrementTypeViewState(
        value = (variable.value?.toLongOrNull() ?: 0).toString(),
    )

    override suspend fun save(temporaryVariableRepository: TemporaryVariableRepository, viewState: VariableTypeViewState) {
        viewState as IncrementTypeViewState
        temporaryVariableRepository.setValue(viewState.value)
    }
}

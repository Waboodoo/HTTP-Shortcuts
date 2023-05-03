package ch.rmy.android.http_shortcuts.activities.variables.editor.types

import ch.rmy.android.http_shortcuts.data.domains.variables.TemporaryVariableRepository
import ch.rmy.android.http_shortcuts.data.models.Variable

class ConstantTypeViewModel : BaseTypeViewModel() {

    override fun createViewState(variable: Variable) = ConstantTypeViewState(
        value = variable.value ?: "",
    )

    override suspend fun save(temporaryVariableRepository: TemporaryVariableRepository, viewState: VariableTypeViewState) {
        viewState as ConstantTypeViewState
        temporaryVariableRepository.setValue(viewState.value)
    }
}

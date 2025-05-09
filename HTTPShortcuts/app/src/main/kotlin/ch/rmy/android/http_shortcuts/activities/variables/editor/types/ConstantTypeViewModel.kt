package ch.rmy.android.http_shortcuts.activities.variables.editor.types

import ch.rmy.android.http_shortcuts.data.domains.variables.TemporaryGlobalVariableRepository
import ch.rmy.android.http_shortcuts.data.models.GlobalVariable

class ConstantTypeViewModel : BaseTypeViewModel() {

    override fun createViewState(variable: GlobalVariable) = ConstantTypeViewState(
        value = variable.value ?: "",
    )

    override suspend fun save(temporaryGlobalVariableRepository: TemporaryGlobalVariableRepository, viewState: VariableTypeViewState) {
        viewState as ConstantTypeViewState
        temporaryGlobalVariableRepository.setValue(viewState.value)
    }
}

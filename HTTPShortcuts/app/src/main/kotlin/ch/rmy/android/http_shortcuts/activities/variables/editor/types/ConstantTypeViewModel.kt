package ch.rmy.android.http_shortcuts.activities.variables.editor.types

import ch.rmy.android.http_shortcuts.data.domains.variables.TemporaryGlobalVariableRepository
import ch.rmy.android.http_shortcuts.data.models.GlobalVariable

class ConstantTypeViewModel : BaseTypeViewModel() {

    override fun createViewState(variable: GlobalVariable) = ConstantTypeViewState(
        value = if (variable.isSecret) {
            SECRET_VALUE
        } else {
            variable.value ?: ""
        },
        isSecret = variable.isSecret,
    )

    override suspend fun save(temporaryGlobalVariableRepository: TemporaryGlobalVariableRepository, viewState: VariableTypeViewState) {
        viewState as ConstantTypeViewState
        if (viewState.value != SECRET_VALUE) {
            temporaryGlobalVariableRepository.setValue(viewState.value)
        }
        temporaryGlobalVariableRepository.setSecret(viewState.isSecret)
    }

    companion object {
        const val SECRET_VALUE = "~**SËçŔËt**~"
    }
}

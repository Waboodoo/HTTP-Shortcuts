package ch.rmy.android.http_shortcuts.activities.variables.editor.types

import ch.rmy.android.http_shortcuts.data.domains.variables.TemporaryGlobalVariableRepository
import ch.rmy.android.http_shortcuts.data.models.GlobalVariable

abstract class BaseTypeViewModel {

    abstract fun createViewState(variable: GlobalVariable): VariableTypeViewState
    open fun validate(viewState: VariableTypeViewState): VariableTypeViewState? = null
    abstract suspend fun save(
        temporaryGlobalVariableRepository: TemporaryGlobalVariableRepository,
        viewState: VariableTypeViewState,
    )
}

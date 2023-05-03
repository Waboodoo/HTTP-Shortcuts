package ch.rmy.android.http_shortcuts.activities.variables.editor.types

import ch.rmy.android.http_shortcuts.data.domains.variables.TemporaryVariableRepository
import ch.rmy.android.http_shortcuts.data.models.Variable

abstract class BaseTypeViewModel {

    abstract fun createViewState(variable: Variable): VariableTypeViewState
    open fun validate(viewState: VariableTypeViewState): VariableTypeViewState? = null
    abstract suspend fun save(
        temporaryVariableRepository: TemporaryVariableRepository,
        viewState: VariableTypeViewState,
    )
}

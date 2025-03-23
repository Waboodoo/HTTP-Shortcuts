package ch.rmy.android.http_shortcuts.variables.types

import ch.rmy.android.http_shortcuts.activities.execute.DialogHandle
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableRepository
import ch.rmy.android.http_shortcuts.data.models.Variable
import javax.inject.Inject

class IncrementType
@Inject
constructor(
    private val variablesRepository: VariableRepository,
) : VariableType {
    override suspend fun resolve(variable: Variable, dialogHandle: DialogHandle): String {
        val previousValue = variable.realValue?.toLongOrNull() ?: 0
        val newValue = (previousValue + 1).toString()
        variablesRepository.setVariableValue(variable.id, newValue)
        return newValue
    }
}

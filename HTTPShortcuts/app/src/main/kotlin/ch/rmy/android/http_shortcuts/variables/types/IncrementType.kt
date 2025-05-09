package ch.rmy.android.http_shortcuts.variables.types

import ch.rmy.android.http_shortcuts.activities.execute.DialogHandle
import ch.rmy.android.http_shortcuts.data.domains.variables.GlobalVariableRepository
import ch.rmy.android.http_shortcuts.data.models.GlobalVariable
import javax.inject.Inject

class IncrementType
@Inject
constructor(
    private val variablesRepository: GlobalVariableRepository,
) : VariableType {
    override suspend fun resolve(variable: GlobalVariable, dialogHandle: DialogHandle): String {
        val previousValue = variable.value?.toLongOrNull() ?: 0
        val newValue = (previousValue + 1).toString()
        variablesRepository.setVariableValue(variable.id, newValue)
        return newValue
    }
}

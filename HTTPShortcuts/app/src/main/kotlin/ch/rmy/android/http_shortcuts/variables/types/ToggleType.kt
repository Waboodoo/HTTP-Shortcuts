package ch.rmy.android.http_shortcuts.variables.types

import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.http_shortcuts.activities.execute.DialogHandle
import ch.rmy.android.http_shortcuts.data.domains.variables.GlobalVariableRepository
import ch.rmy.android.http_shortcuts.data.models.GlobalVariable
import javax.inject.Inject

class ToggleType
@Inject
constructor(
    private val variablesRepository: GlobalVariableRepository,
) : VariableType {
    override suspend fun resolve(variable: GlobalVariable, dialogHandle: DialogHandle): String {
        val options = variable.getStringListData(KEY_VALUES)?.takeUnlessEmpty() ?: return ""

        val index = variablesRepository.updateVariableValue(variable.id) { previousValue ->
            getNextIndex(previousValue, options.size).toString()
        }?.toIntOrNull() ?: getNextIndex(variable.value, options.size)
        return options[index]
    }

    private fun getNextIndex(previousValue: String?, numberOfOptions: Int): Int {
        val previousIndex = previousValue?.toIntOrNull()?.coerceAtLeast(0) ?: 0
        return (previousIndex + 1) % numberOfOptions
    }

    companion object {
        const val KEY_VALUES = "values"
    }
}

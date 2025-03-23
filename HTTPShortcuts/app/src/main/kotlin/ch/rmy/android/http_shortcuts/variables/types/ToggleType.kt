package ch.rmy.android.http_shortcuts.variables.types

import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.http_shortcuts.activities.execute.DialogHandle
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableRepository
import ch.rmy.android.http_shortcuts.data.models.Variable
import javax.inject.Inject

class ToggleType
@Inject
constructor(
    private val variablesRepository: VariableRepository,
) : VariableType {
    override suspend fun resolve(variable: Variable, dialogHandle: DialogHandle): String {
        val options = variable.getStringListData(KEY_VALUES)?.takeUnlessEmpty() ?: return ""

        val previousIndex = variable.realValue?.toIntOrNull()?.coerceAtLeast(0) ?: 0
        val index = (previousIndex + 1) % options.size
        variablesRepository.setVariableValue(variable.id, index.toString())
        return options[index]
    }

    companion object {
        const val KEY_VALUES = "values"
    }
}

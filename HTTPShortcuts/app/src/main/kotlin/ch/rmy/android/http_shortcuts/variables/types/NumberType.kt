package ch.rmy.android.http_shortcuts.variables.types

import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.framework.extensions.toLocalizable
import ch.rmy.android.http_shortcuts.activities.execute.DialogHandle
import ch.rmy.android.http_shortcuts.activities.execute.ExecuteDialogState
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableRepository
import ch.rmy.android.http_shortcuts.data.models.Variable
import javax.inject.Inject

class NumberType
@Inject
constructor(
    private val variablesRepository: VariableRepository,
) : VariableType {
    override suspend fun resolve(variable: Variable, dialogHandle: DialogHandle): String {
        val value = dialogHandle.showDialog(
            ExecuteDialogState.TextInput(
                title = variable.title.takeUnlessEmpty()?.toLocalizable(),
                message = variable.message.takeUnlessEmpty()?.toLocalizable(),
                initialValue = variable.realValue?.takeIf { variable.rememberValue } ?: "",
                type = ExecuteDialogState.TextInput.Type.NUMBER,
            ),
        )
        if (variable.rememberValue) {
            variablesRepository.setVariableValue(variable.id, value)
        }
        return value
    }
}

package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.framework.extensions.toLocalizable
import ch.rmy.android.http_shortcuts.activities.execute.ExecuteDialogState
import ch.rmy.android.http_shortcuts.exceptions.DialogCancellationException
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.http_shortcuts.variables.Variables

class PromptNumberAction(private val message: String, private val prefill: String) : BaseAction() {

    override suspend fun execute(executionContext: ExecutionContext): Double? {
        val finalMessage = Variables.rawPlaceholdersToResolvedValues(
            message,
            executionContext.variableManager.getVariableValuesByIds(),
        )

        if (finalMessage.isEmpty()) {
            return null
        }

        return try {
            executionContext.dialogHandle.showDialog(
                ExecuteDialogState.TextInput(
                    message = finalMessage.toLocalizable(),
                    type = ExecuteDialogState.TextInput.Type.NUMBER,
                    initialValue = prefill,
                )
            )
                .toDoubleOrNull()
                ?: Double.NaN
        } catch (e: DialogCancellationException) {
            null
        }
    }
}

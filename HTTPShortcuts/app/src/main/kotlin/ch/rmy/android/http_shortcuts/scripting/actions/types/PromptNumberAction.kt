package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.framework.extensions.toLocalizable
import ch.rmy.android.http_shortcuts.activities.execute.ExecuteDialogState
import ch.rmy.android.http_shortcuts.exceptions.DialogCancellationException
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.http_shortcuts.variables.Variables
import javax.inject.Inject

class PromptNumberAction
@Inject
constructor() : Action<PromptNumberAction.Params> {
    override suspend fun Params.execute(executionContext: ExecutionContext): Double? {
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
                ),
            )
                .toDoubleOrNull()
                ?: Double.NaN
        } catch (e: DialogCancellationException) {
            null
        }
    }

    data class Params(
        val message: String,
        val prefill: String,
    )
}

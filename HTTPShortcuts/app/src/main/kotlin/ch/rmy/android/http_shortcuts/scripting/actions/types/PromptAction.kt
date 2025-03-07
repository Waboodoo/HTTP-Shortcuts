package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.framework.extensions.toLocalizable
import ch.rmy.android.http_shortcuts.activities.execute.ExecuteDialogState
import ch.rmy.android.http_shortcuts.exceptions.DialogCancellationException
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.http_shortcuts.variables.Variables
import javax.inject.Inject

class PromptAction
@Inject
constructor() : Action<PromptAction.Params> {
    override suspend fun Params.execute(executionContext: ExecutionContext): String? {
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
                    type = ExecuteDialogState.TextInput.Type.TEXT,
                    initialValue = prefill,
                ),
            )
        } catch (e: DialogCancellationException) {
            null
        }
    }

    data class Params(
        val message: String,
        val prefill: String,
    )
}

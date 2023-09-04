package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.http_shortcuts.activities.execute.ExecuteDialogState
import ch.rmy.android.http_shortcuts.exceptions.DialogCancellationException
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.http_shortcuts.variables.Variables
import javax.inject.Inject

class DialogAction
@Inject
constructor() : Action<DialogAction.Params> {
    override suspend fun Params.execute(executionContext: ExecutionContext) {
        val finalMessage = Variables.rawPlaceholdersToResolvedValues(
            message,
            executionContext.variableManager.getVariableValuesByIds(),
        )
        if (finalMessage.isEmpty()) {
            return
        }

        try {
            executionContext.dialogHandle.showDialog(
                ExecuteDialogState.RichTextDisplay(
                    title = title.takeUnlessEmpty(),
                    message = finalMessage,
                )
            )
        } catch (e: DialogCancellationException) {
            // proceed as normal
        }
    }

    data class Params(
        val message: String,
        val title: String,
    )
}

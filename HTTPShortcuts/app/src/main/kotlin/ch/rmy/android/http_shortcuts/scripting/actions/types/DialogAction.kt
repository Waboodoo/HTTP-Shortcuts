package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.http_shortcuts.activities.execute.ExecuteDialogState
import ch.rmy.android.http_shortcuts.activities.execute.ExecuteDialogState.RichTextDisplay.ButtonResult
import ch.rmy.android.http_shortcuts.exceptions.DialogCancellationException
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.http_shortcuts.variables.Variables
import ch.rmy.android.scripting.JsObject
import javax.inject.Inject

class DialogAction
@Inject
constructor() : Action<DialogAction.Params> {
    override suspend fun Params.execute(executionContext: ExecutionContext): JsObject {
        val finalMessage = Variables.rawPlaceholdersToResolvedValues(
            message,
            executionContext.variableManager.getVariableValuesByIds(),
        )
        val result = if (finalMessage.isEmpty()) {
            "not-shown"
        } else {
            try {
                val buttonResult = executionContext.dialogHandle.showDialog(
                    ExecuteDialogState.RichTextDisplay(
                        title = title.takeUnlessEmpty(),
                        message = finalMessage,
                        buttons = buttons?.takeUnlessEmpty(),
                    ),
                )
                when (buttonResult) {
                    ButtonResult.OK -> "ok"
                    ButtonResult.BUTTON1 -> "button1"
                    ButtonResult.BUTTON2 -> "button2"
                }
            } catch (_: DialogCancellationException) {
                // proceed as normal
                "cancelled"
            }
        }
        return executionContext.scriptingEngine.buildJsObject {
            property("result", result)
        }
    }

    data class Params(
        val message: String,
        val title: String,
        val buttons: List<String>?,
    )
}

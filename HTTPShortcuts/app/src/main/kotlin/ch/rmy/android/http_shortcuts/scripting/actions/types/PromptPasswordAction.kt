package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.framework.extensions.toLocalizable
import ch.rmy.android.http_shortcuts.activities.execute.ExecuteDialogState
import ch.rmy.android.http_shortcuts.exceptions.DialogCancellationException
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import javax.inject.Inject

class PromptPasswordAction
@Inject
constructor() : Action<PromptPasswordAction.Params> {
    override suspend fun Params.execute(executionContext: ExecutionContext): String? =
        try {
            executionContext.dialogHandle.showDialog(
                ExecuteDialogState.TextInput(
                    message = message.toLocalizable(),
                    type = ExecuteDialogState.TextInput.Type.PASSWORD,
                    initialValue = prefill,
                ),
            )
        } catch (e: DialogCancellationException) {
            null
        }

    data class Params(
        val message: String,
        val prefill: String,
    )
}

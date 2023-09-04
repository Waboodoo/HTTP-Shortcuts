package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.activities.execute.ExecuteDialogState
import ch.rmy.android.http_shortcuts.exceptions.DialogCancellationException
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.http_shortcuts.utils.ColorUtil.colorIntToHexString
import ch.rmy.android.http_shortcuts.utils.ColorUtil.hexStringToColorInt
import javax.inject.Inject

class PromptColorAction
@Inject
constructor() : Action<PromptColorAction.Params> {
    override suspend fun Params.execute(executionContext: ExecutionContext): String? =
        try {
            executionContext.dialogHandle.showDialog(
                ExecuteDialogState.ColorPicker(
                    initialColor = initialColor?.trimStart('#')?.hexStringToColorInt(),
                )
            )
                .colorIntToHexString()
        } catch (e: DialogCancellationException) {
            null
        }

    data class Params(
        val initialColor: String?,
    )
}

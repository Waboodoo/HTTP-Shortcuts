package ch.rmy.android.http_shortcuts.activities.execute

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.ColorPickerDialog
import ch.rmy.android.http_shortcuts.components.ConfirmDialog
import ch.rmy.android.http_shortcuts.components.MessageDialog
import ch.rmy.android.http_shortcuts.extensions.localize

@Composable
fun ExecuteDialogs(
    dialogState: ExecuteDialogState?,
    onResult: (Any) -> Unit,
    onDismissed: () -> Unit,
) {
    when (dialogState) {
        is ExecuteDialogState.GenericMessage -> {
            MessageDialog(
                title = dialogState.title?.localize(),
                message = dialogState.message.localize(),
                onDismissRequest = onDismissed,
            )
        }
        is ExecuteDialogState.GenericConfirm -> {
            ConfirmDialog(
                title = dialogState.title?.localize(),
                message = dialogState.message.localize(),
                confirmButton = dialogState.confirmButton?.localize() ?: stringResource(R.string.dialog_ok),
                onConfirmRequest = {
                    onResult(Unit)
                },
                onDismissRequest = onDismissed,
            )
        }
        is ExecuteDialogState.ColorPicker -> {
            ColorPickerDialog(
                title = dialogState.title?.localize(),
                initialColor = dialogState.initialColor,
                onColorSelected = {
                    onResult(it)
                },
                onDismissRequested = onDismissed,
            )
        }
        null -> Unit
    }
}

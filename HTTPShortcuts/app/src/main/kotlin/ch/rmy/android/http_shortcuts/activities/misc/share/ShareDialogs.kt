package ch.rmy.android.http_shortcuts.activities.misc.share

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.MessageDialog
import ch.rmy.android.http_shortcuts.components.ProgressDialog
import ch.rmy.android.http_shortcuts.components.ShortcutPickerDialog
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId

@Composable
fun ShareDialogs(
    dialogState: ShareDialogState?,
    onShortcutSelected: (ShortcutId) -> Unit,
    onDismissed: () -> Unit,
) {
    when (dialogState) {
        ShareDialogState.Progress -> {
            ProgressDialog(
                text = stringResource(R.string.generic_processing_in_progress),
                onDismissRequest = onDismissed,
            )
        }
        ShareDialogState.Instructions -> {
            MessageDialog(
                message = stringResource(R.string.error_not_suitable_shortcuts),
                onDismissRequest = onDismissed,
            )
        }
        is ShareDialogState.PickShortcut -> {
            ShortcutPickerDialog(
                shortcuts = dialogState.shortcuts,
                onShortcutSelected = onShortcutSelected,
                onDismissRequested = onDismissed,
            )
        }
        null -> Unit
    }
}

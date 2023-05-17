package ch.rmy.android.http_shortcuts.activities.editor.shortcuts

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.ConfirmDialog
import ch.rmy.android.http_shortcuts.components.MessageDialog
import ch.rmy.android.http_shortcuts.components.ShortcutsPickerDialog
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.dtos.ShortcutPlaceholder

@Composable
fun TriggerShortcutsDialogs(
    dialogState: TriggerShortcutsDialogState?,
    onShortcutAddConfirmed: (List<ShortcutId>) -> Unit,
    onShortcutRemoveConfirmed: () -> Unit,
    onDismissed: () -> Unit,
) {
    when (dialogState) {
        is TriggerShortcutsDialogState.AddShortcuts -> {
            AddShortcutsDialog(
                shortcuts = dialogState.shortcuts,
                onConfirmed = onShortcutAddConfirmed,
                onDismissed = onDismissed,
            )
        }
        is TriggerShortcutsDialogState.DeleteShortcut -> {
            RemoveShortcutDialog(
                name = dialogState.name,
                onConfirmed = onShortcutRemoveConfirmed,
                onDismissed = onDismissed,
            )
        }
        null -> Unit
    }
}

@Composable
private fun AddShortcutsDialog(
    shortcuts: List<ShortcutPlaceholder>,
    onConfirmed: (List<ShortcutId>) -> Unit,
    onDismissed: () -> Unit,
) {
    if (shortcuts.isEmpty()) {
        MessageDialog(
            title = stringResource(R.string.title_add_trigger_shortcut),
            message = stringResource(R.string.error_add_trigger_shortcut_no_shortcuts),
            onDismissRequest = onDismissed,
        )
        return
    }

    ShortcutsPickerDialog(
        title = stringResource(R.string.title_add_trigger_shortcut),
        shortcuts = shortcuts,
        allowEmpty = false,
        onConfirmed = onConfirmed,
        onDismissRequested = onDismissed,
    )
}

@Composable
private fun RemoveShortcutDialog(
    name: String?,
    onConfirmed: () -> Unit,
    onDismissed: () -> Unit,
) {
    ConfirmDialog(
        title = stringResource(R.string.title_remove_trigger_shortcut),
        message = if (name != null) {
            stringResource(R.string.message_remove_trigger_shortcut, name)
        } else {
            stringResource(R.string.message_remove_deleted_trigger_shortcut)
        },
        confirmButton = stringResource(R.string.dialog_remove),
        onConfirmRequest = onConfirmed,
        onDismissRequest = onDismissed,
    )
}

package ch.rmy.android.http_shortcuts.activities.misc.second_launcher

import androidx.compose.runtime.Composable
import ch.rmy.android.http_shortcuts.components.ShortcutPickerDialog
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId

@Composable
fun SecondLauncherDialogs(
    dialogState: SecondLauncherDialogState?,
    onShortcutSelected: (ShortcutId) -> Unit,
    onDismissed: () -> Unit,
) {
    when (dialogState) {
        is SecondLauncherDialogState.PickShortcut -> {
            ShortcutPickerDialog(
                shortcuts = dialogState.shortcuts,
                onShortcutSelected = onShortcutSelected,
                onDismissRequested = onDismissed,
            )
        }
        null -> Unit
    }
}

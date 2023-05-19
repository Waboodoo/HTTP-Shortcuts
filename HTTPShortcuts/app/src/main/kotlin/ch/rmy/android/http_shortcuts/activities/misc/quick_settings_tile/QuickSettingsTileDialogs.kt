package ch.rmy.android.http_shortcuts.activities.misc.quick_settings_tile

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.MessageDialog
import ch.rmy.android.http_shortcuts.components.ShortcutPickerDialog
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId

@Composable
fun QuickSettingsTileDialogs(
    dialogState: QuickSettingsTileDialogState?,
    onShortcutSelected: (ShortcutId) -> Unit,
    onDismissed: () -> Unit,
) {
    when (dialogState) {
        is QuickSettingsTileDialogState.PickShortcut -> {
            ShortcutPickerDialog(
                shortcuts = dialogState.shortcuts,
                onShortcutSelected = onShortcutSelected,
                onDismissRequested = onDismissed,
            )
        }
        is QuickSettingsTileDialogState.Instructions -> {
            MessageDialog(
                message = stringResource(
                    R.string.instructions_quick_settings_tile,
                    stringResource(R.string.label_quick_tile_shortcut),
                    stringResource(R.string.label_execution_settings),
                ),
                onDismissRequest = onDismissed,
            )
        }
        null -> Unit
    }
}

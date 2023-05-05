package ch.rmy.android.http_shortcuts.activities.editor

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.DiscardWarningDialog
import ch.rmy.android.http_shortcuts.components.IconPickerDialog
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon

@Composable
fun ShortcutEditorDialogs(
    dialogState: ShortcutEditorDialogState?,
    onDiscardConfirmed: () -> Unit,
    onIconSelected: (ShortcutIcon) -> Unit,
    onFaviconOptionSelected: () -> Unit,
    onDismiss: () -> Unit,
) {
    when (dialogState) {
        is ShortcutEditorDialogState.DiscardWarning -> {
            DiscardWarningDialog(
                onConfirmed = onDiscardConfirmed,
                onDismissRequested = onDismiss,
            )
        }
        is ShortcutEditorDialogState.PickIcon -> {
            IconPickerDialog(
                title = stringResource(R.string.change_icon),
                onIconSelected = onIconSelected,
                onFaviconOptionSelected = onFaviconOptionSelected.takeIf { dialogState.includeFaviconOption },
                onDismissRequested = onDismiss,
            )
        }
        null -> Unit
    }
}

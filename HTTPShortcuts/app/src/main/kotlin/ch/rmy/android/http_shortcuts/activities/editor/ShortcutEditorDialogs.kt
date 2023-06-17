package ch.rmy.android.http_shortcuts.activities.editor

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ch.rmy.android.framework.extensions.launch
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.icons.IconPickerActivity
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
    val pickCustomIcon = rememberLauncherForActivityResult(IconPickerActivity.PickIcon) { icon ->
        if (icon != null) {
            onIconSelected(icon)
        } else {
            onDismiss()
        }
    }

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
                onCustomIconOptionSelected = {
                    pickCustomIcon.launch()
                },
                onIconSelected = onIconSelected,
                onFaviconOptionSelected = onFaviconOptionSelected.takeIf { dialogState.includeFaviconOption },
                onDismissRequested = onDismiss,
            )
        }
        null -> Unit
    }
}

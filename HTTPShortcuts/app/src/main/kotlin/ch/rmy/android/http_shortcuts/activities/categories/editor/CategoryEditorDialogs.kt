package ch.rmy.android.http_shortcuts.activities.categories.editor

import androidx.compose.runtime.Composable
import ch.rmy.android.http_shortcuts.components.ColorPickerDialog
import ch.rmy.android.http_shortcuts.components.DiscardWarningDialog

@Composable
fun CategoryEditorDialogs(
    dialogState: CategoryEditorDialogState?,
    onColorSelected: (Int) -> Unit,
    onDiscardConfirmed: () -> Unit,
    onDismissRequested: () -> Unit,
) {
    when (dialogState) {
        is CategoryEditorDialogState.ColorPicker -> {
            ColorPickerDialog(
                initialColor = dialogState.initialColor,
                onColorSelected = onColorSelected,
                onDismissRequested = onDismissRequested,
            )
        }
        is CategoryEditorDialogState.DiscardWarning -> {
            DiscardWarningDialog(
                onConfirmed = onDiscardConfirmed,
                onDismissRequested = onDismissRequested,
            )
        }
        null -> Unit
    }
}

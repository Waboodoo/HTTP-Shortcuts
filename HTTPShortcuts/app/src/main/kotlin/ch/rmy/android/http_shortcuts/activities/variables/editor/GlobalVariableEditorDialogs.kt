package ch.rmy.android.http_shortcuts.activities.variables.editor

import androidx.compose.runtime.Composable
import ch.rmy.android.http_shortcuts.components.DiscardWarningDialog

@Composable
fun GlobalVariableEditorDialogs(
    dialogState: GlobalVariableEditorDialogState?,
    onDiscardDialogConfirmed: () -> Unit,
    onDismissed: () -> Unit,
) {
    when (dialogState) {
        is GlobalVariableEditorDialogState.DiscardWarning -> {
            DiscardWarningDialog(
                onConfirmed = onDiscardDialogConfirmed,
                onDismissRequested = onDismissed,
            )
        }
        null -> Unit
    }
}

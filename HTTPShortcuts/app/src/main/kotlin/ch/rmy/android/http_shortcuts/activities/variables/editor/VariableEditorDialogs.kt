package ch.rmy.android.http_shortcuts.activities.variables.editor

import androidx.compose.runtime.Composable
import ch.rmy.android.http_shortcuts.components.DiscardWarningDialog

@Composable
fun VariableEditorDialogs(
    dialogState: VariableEditorDialogState?,
    onDiscardDialogConfirmed: () -> Unit,
    onDismissed: () -> Unit,
) {
    when (dialogState) {
        is VariableEditorDialogState.DiscardWarning -> {
            DiscardWarningDialog(
                onConfirmed = onDiscardDialogConfirmed,
                onDismissRequested = onDismissed,
            )
        }
        null -> Unit
    }
}

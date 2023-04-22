package ch.rmy.android.http_shortcuts.activities.globalcode

import androidx.compose.runtime.Composable
import ch.rmy.android.http_shortcuts.components.DiscardWarningDialog

@Composable
fun GlobalScriptingDialogs(
    dialogState: GlobalScriptingDialogState?,
    onDiscardConfirmed: () -> Unit,
    onDismissRequested: () -> Unit,
) {
    when (dialogState) {
        GlobalScriptingDialogState.DiscardWarning -> {
            DiscardWarningDialog(
                onConfirmed = onDiscardConfirmed,
                onDismissRequested = onDismissRequested,
            )
        }
        null -> Unit
    }
}

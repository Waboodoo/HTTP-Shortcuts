package ch.rmy.android.http_shortcuts.activities.importexport

import androidx.compose.runtime.Composable
import ch.rmy.android.http_shortcuts.components.MessageDialog
import ch.rmy.android.http_shortcuts.components.ProgressDialog
import ch.rmy.android.http_shortcuts.extensions.localize

@Composable
fun ExportDialog(
    dialogState: ExportDialogState?,
    onDismissRequest: () -> Unit,
) {
    when (dialogState) {
        is ExportDialogState.Error -> {
            MessageDialog(dialogState.message.localize(), onDismissRequest = onDismissRequest)
        }
        is ExportDialogState.Progress -> {
            ProgressDialog(dialogState.text.localize(), onDismissRequest)
        }
        null -> Unit
    }
}

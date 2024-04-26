package ch.rmy.android.http_shortcuts.activities.importexport

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.MessageDialog
import ch.rmy.android.http_shortcuts.components.ProgressDialog
import ch.rmy.android.http_shortcuts.components.SelectDialog
import ch.rmy.android.http_shortcuts.components.SelectDialogEntry
import ch.rmy.android.http_shortcuts.extensions.localize

@Composable
fun ExportDialog(
    dialogState: ExportDialogState?,
    onExportToFileOptionSelected: () -> Unit,
    onExportViaSharingOptionSelected: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    when (dialogState) {
        is ExportDialogState.Error -> {
            MessageDialog(dialogState.message.localize(), onDismissRequest = onDismissRequest)
        }
        is ExportDialogState.Progress -> {
            ProgressDialog(dialogState.text.localize(), onDismissRequest)
        }
        is ExportDialogState.SelectExportDestinationDialog -> {
            SelectExportDestinationDialog(
                onExportToFileOptionSelected = onExportToFileOptionSelected,
                onExportViaSharingOptionSelected = onExportViaSharingOptionSelected,
                onDismissRequest = onDismissRequest,
            )
        }
        null -> Unit
    }
}

@Composable
private fun SelectExportDestinationDialog(
    onExportToFileOptionSelected: () -> Unit,
    onExportViaSharingOptionSelected: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    SelectDialog(
        title = stringResource(R.string.title_export),
        onDismissRequest = onDismissRequest,
    ) {
        SelectDialogEntry(
            label = stringResource(R.string.button_export_to_general),
            onClick = onExportToFileOptionSelected,
        )
        SelectDialogEntry(
            label = stringResource(R.string.button_export_send_to),
            onClick = onExportViaSharingOptionSelected,
        )
    }
}

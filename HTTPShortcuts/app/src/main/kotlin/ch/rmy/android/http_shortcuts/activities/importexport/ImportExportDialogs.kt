package ch.rmy.android.http_shortcuts.activities.importexport

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.MenuDialogEntry
import ch.rmy.android.http_shortcuts.components.MessageDialog
import ch.rmy.android.http_shortcuts.components.MultiSelectDialog
import ch.rmy.android.http_shortcuts.components.ProgressDialog
import ch.rmy.android.http_shortcuts.components.SelectDialog
import ch.rmy.android.http_shortcuts.components.TextInputDialog
import ch.rmy.android.http_shortcuts.components.models.MenuEntry
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.extensions.localize

@Composable
fun ImportExportDialog(
    dialogState: ImportExportDialogState?,
    onImportFromUrl: (String) -> Unit,
    onShortcutsSelectedForExport: (List<ShortcutId>) -> Unit,
    onExportToFileOptionSelected: () -> Unit,
    onExportViaSharingOptionSelected: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    when (dialogState) {
        is ImportExportDialogState.Error -> {
            MessageDialog(dialogState.message.localize(), onDismissRequest)
        }
        is ImportExportDialogState.Progress -> {
            ProgressDialog(dialogState.text.localize(), onDismissRequest)
        }
        is ImportExportDialogState.ImportFromUrl -> {
            ImportFromUrlDialog(dialogState.initialValue, onImportFromUrl, onDismissRequest)
        }
        is ImportExportDialogState.ShortcutSelectionForExport -> {
            SelectShortcutsForExportDialog(dialogState.entries, onShortcutsSelectedForExport, onDismissRequest)
        }
        is ImportExportDialogState.SelectExportDestinationDialog -> {
            SelectExportDestinationDialog(onExportToFileOptionSelected, onExportViaSharingOptionSelected, onDismissRequest)
        }
        null -> Unit
    }
}

@Composable
private fun ImportFromUrlDialog(initialValue: String, onImportFromUrl: (String) -> Unit, onDismissRequest: () -> Unit) {
    TextInputDialog(
        title = stringResource(R.string.dialog_title_import_from_url),
        allowEmpty = false,
        initialValue = initialValue,
        keyboardType = KeyboardType.Uri,
        onDismissRequest = { newValue ->
            if (newValue != null) {
                onImportFromUrl(newValue)
            } else {
                onDismissRequest()
            }
        },
    )
}

@Composable
private fun SelectShortcutsForExportDialog(
    entries: List<MenuEntry<ShortcutId>>,
    onShortcutsForExportSelected: (selectedShortcutIds: List<ShortcutId>) -> Unit,
    onDismissRequest: () -> Unit,
) {
    MultiSelectDialog(
        title = stringResource(R.string.dialog_title_select_shortcuts_for_export),
        entries = entries,
        allChecked = true,
        confirmButtonLabel = stringResource(R.string.dialog_button_export),
        dismissButton = {
            TextButton(
                onClick = onDismissRequest,
            ) {
                Text(stringResource(R.string.dialog_cancel))
            }
        },
        onDismissRequest = { selectedShortcutIds ->
            if (selectedShortcutIds != null) {
                onShortcutsForExportSelected(selectedShortcutIds)
            } else {
                onDismissRequest()
            }
        }
    )
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
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            MenuDialogEntry(
                label = stringResource(R.string.button_export_to_general),
                onClick = onExportToFileOptionSelected,
            )
            MenuDialogEntry(
                label = stringResource(R.string.button_export_send_to),
                onClick = onExportViaSharingOptionSelected,
            )
        }
    }
}

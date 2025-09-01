package ch.rmy.android.http_shortcuts.activities.importexport

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.MessageDialog
import ch.rmy.android.http_shortcuts.components.ProgressDialog
import ch.rmy.android.http_shortcuts.components.TextInputDialog
import ch.rmy.android.http_shortcuts.extensions.localize

@Composable
fun ImportExportDialog(
    dialogState: ImportExportDialogState?,
    onImportPasswordSubmitted: (String) -> Unit,
    onDismissRequest: () -> Unit,
) {
    when (dialogState) {
        is ImportExportDialogState.Error -> {
            MessageDialog(dialogState.message.localize(), onDismissRequest = onDismissRequest)
        }
        is ImportExportDialogState.Progress -> {
            ProgressDialog(dialogState.text.localize(), onDismissRequest)
        }
        is ImportExportDialogState.ImportPasswordPrompt -> {
            ImportPasswordDialog(
                tryAgain = dialogState.tryAgain,
                onSubmitted = onImportPasswordSubmitted,
                onDismissed = onDismissRequest,
            )
        }
        null -> Unit
    }
}

@Composable
private fun ImportPasswordDialog(
    tryAgain: Boolean,
    onSubmitted: (String) -> Unit,
    onDismissed: () -> Unit,
) {
    TextInputDialog(
        title = stringResource(R.string.dialog_title_import),
        message = stringResource(if (tryAgain) R.string.dialog_text_import_with_password_retry else R.string.dialog_text_import_with_password),
        confirmButton = stringResource(R.string.dialog_ok),
        allowEmpty = false,
        onDismissRequest = {
            if (it != null) {
                onSubmitted(it)
            } else {
                onDismissed()
            }
        },
        keyboardType = KeyboardType.Password,
    )
}

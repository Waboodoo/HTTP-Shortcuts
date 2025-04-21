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
fun ExportDialog(
    dialogState: ExportDialogState?,
    onPasswordSubmitted: (String) -> Unit,
    onDismissRequest: () -> Unit,
) {
    when (dialogState) {
        is ExportDialogState.Error -> {
            MessageDialog(dialogState.message.localize(), onDismissRequest = onDismissRequest)
        }
        is ExportDialogState.Progress -> {
            ProgressDialog(dialogState.text.localize(), onDismissRequest)
        }
        is ExportDialogState.ExportPasswordPrompt -> {
            ExportPasswordDialog(
                initialPassword = dialogState.password,
                onSubmitted = onPasswordSubmitted,
                onDismissed = onDismissRequest,
            )
        }
        null -> Unit
    }
}

@Composable
private fun ExportPasswordDialog(
    initialPassword: String,
    onSubmitted: (String) -> Unit,
    onDismissed: () -> Unit,
) {
    TextInputDialog(
        title = stringResource(R.string.dialog_title_export_encryption),
        message = stringResource(R.string.dialog_text_export_with_password),
        confirmButton = stringResource(R.string.dialog_ok),
        initialValue = initialPassword,
        allowEmpty = true,
        onDismissRequest = {
            if (it != null) {
                onSubmitted(it)
            } else {
                onDismissed()
            }
        },
        keyboardType = KeyboardType.Password,
        singleLine = true,
    )
}

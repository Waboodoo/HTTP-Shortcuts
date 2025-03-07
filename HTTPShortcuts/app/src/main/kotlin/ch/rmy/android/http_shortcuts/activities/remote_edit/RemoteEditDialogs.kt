package ch.rmy.android.http_shortcuts.activities.remote_edit

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.MessageDialog
import ch.rmy.android.http_shortcuts.components.ProgressDialog
import ch.rmy.android.http_shortcuts.components.TextInputDialog
import ch.rmy.android.http_shortcuts.extensions.localize

@Composable
fun RemoteEditDialog(dialogState: RemoteEditDialogState?, onDismissRequest: () -> Unit, onServerUrlChange: (String) -> Unit) {
    when (dialogState) {
        is RemoteEditDialogState.Error -> {
            MessageDialog(dialogState.message.localize(), onDismissRequest = onDismissRequest)
        }
        is RemoteEditDialogState.Progress -> {
            ProgressDialog(dialogState.text.localize(), onDismissRequest = onDismissRequest)
        }
        is RemoteEditDialogState.EditServerUrl -> {
            EditServerUrlDialog(
                currentServerUrl = dialogState.currentServerAddress,
                onDismissRequest = { newUrl ->
                    if (newUrl != null) {
                        onServerUrlChange(newUrl)
                    } else {
                        onDismissRequest()
                    }
                },
            )
        }
        null -> Unit
    }
}

@Composable
private fun EditServerUrlDialog(currentServerUrl: String, onDismissRequest: (newUrl: String?) -> Unit) {
    TextInputDialog(
        title = stringResource(R.string.title_change_remote_server),
        allowEmpty = false,
        initialValue = currentServerUrl,
        keyboardType = KeyboardType.Uri,
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest("")
                },
            ) {
                Text(stringResource(R.string.dialog_reset))
            }
        },
        onDismissRequest = onDismissRequest,
    )
}

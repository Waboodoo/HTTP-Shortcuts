package ch.rmy.android.http_shortcuts.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import ch.rmy.android.http_shortcuts.R

@Composable
fun ConfirmDialog(
    message: String,
    title: String? = null,
    confirmButton: String = stringResource(R.string.dialog_ok),
    onConfirmRequest: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    AlertDialog(
        modifier = Modifier.padding(Spacing.MEDIUM),
        properties = DialogProperties(usePlatformDefaultWidth = false),
        onDismissRequest = onDismissRequest,
        title = title?.let {
            {
                Text(it)
            }
        },
        text = {
            Text(message)
        },
        confirmButton = {
            TextButton(onClick = onConfirmRequest) {
                Text(confirmButton)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.dialog_cancel))
            }
        },
    )
}

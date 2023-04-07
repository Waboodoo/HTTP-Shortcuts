package ch.rmy.android.http_shortcuts.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun SelectDialog(
    title: String,
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit,
) {
    AlertDialog(
        onDismissRequest = {
            onDismissRequest()
        },
        title = {
            Text(title)
        },
        text = {
            content()
        },
        confirmButton = {},
    )
}

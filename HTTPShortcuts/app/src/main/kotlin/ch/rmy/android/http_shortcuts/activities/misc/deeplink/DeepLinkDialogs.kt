package ch.rmy.android.http_shortcuts.activities.misc.deeplink

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.MessageDialog

@Composable
fun DeepLinkDialogs(
    dialogState: DeepLinkDialogState?,
    onDismissed: () -> Unit,
) {
    when (dialogState) {
        DeepLinkDialogState.Instructions -> {
            AlertDialog(
                onDismissRequest = onDismissed,
                text = {
                    Text(
                        buildAnnotatedString {
                            val prefix = "http-shortcuts://"
                            val exampleUrl = "$prefix<Name/ID of Shortcut>"
                            val text = stringResource(R.string.instructions_deep_linking, exampleUrl)
                            append(text)
                            val start = text.indexOf(exampleUrl)
                            addStyle(SpanStyle(fontWeight = FontWeight.Bold), start + prefix.length, start + exampleUrl.length)
                        },
                    )
                },
                confirmButton = {
                    TextButton(onClick = onDismissed) {
                        Text(stringResource(R.string.dialog_ok))
                    }
                },
            )
        }
        is DeepLinkDialogState.ShortcutNotFound -> {
            MessageDialog(
                message = stringResource(R.string.error_shortcut_not_found_for_deep_link, dialogState.shortcutNameOrId),
                onDismissRequest = onDismissed,
            )
        }
        null -> Unit
    }
}

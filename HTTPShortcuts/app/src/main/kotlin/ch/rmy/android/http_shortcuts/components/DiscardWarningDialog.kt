package ch.rmy.android.http_shortcuts.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ch.rmy.android.http_shortcuts.R

@Composable
fun DiscardWarningDialog(
    onConfirmed: () -> Unit,
    onDismissRequested: () -> Unit,
) {
    ConfirmDialog(
        message = stringResource(R.string.confirm_discard_changes_message),
        confirmButton = stringResource(R.string.dialog_discard),
        onConfirmRequest = onConfirmed,
        onDismissRequest = onDismissRequested,
    )
}

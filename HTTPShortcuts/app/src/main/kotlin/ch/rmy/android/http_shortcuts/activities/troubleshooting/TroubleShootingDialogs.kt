package ch.rmy.android.http_shortcuts.activities.troubleshooting

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.ConfirmDialog

@Composable
fun TroubleShootingDialogs(
    dialogState: TroubleShootingDialogState?,
    onClearCookiesConfirmed: () -> Unit,
    onDismissalRequested: () -> Unit,
) {
    when (dialogState) {
        is TroubleShootingDialogState.ClearCookies -> {
            ClearCookiesDialog(
                onConfirm = onClearCookiesConfirmed,
                onDismissalRequested = onDismissalRequested,
            )
        }
        null -> Unit
    }
}

@Composable
private fun ClearCookiesDialog(
    onConfirm: () -> Unit,
    onDismissalRequested: () -> Unit,
) {
    ConfirmDialog(
        message = stringResource(R.string.confirm_clear_cookies_message),
        confirmButton = stringResource(R.string.dialog_delete),
        onConfirmRequest = onConfirm,
        onDismissRequest = onDismissalRequested,
    )
}

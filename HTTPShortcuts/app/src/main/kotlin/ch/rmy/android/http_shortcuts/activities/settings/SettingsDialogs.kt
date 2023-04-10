package ch.rmy.android.http_shortcuts.activities.settings

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.ConfirmDialog
import ch.rmy.android.http_shortcuts.components.TextInputDialog

@Composable
fun SettingsDialogs(
    dialogState: SettingsDialogState?,
    onLockConfirmed: (String) -> Unit,
    onTitleChangeConfirmed: (String) -> Unit,
    onClearCookiesConfirmed: () -> Unit,
    onDismissalRequested: () -> Unit,
) {
    when (dialogState) {
        is SettingsDialogState.ChangeTitle -> {
            ChangeTitleDialog(
                dialogState.oldTitle,
                onTitleChangeConfirmed,
                onDismissalRequested,
            )
        }
        is SettingsDialogState.LockApp -> {
            LockAppDialog(
                onLockConfirmed,
                onDismissalRequested,
            )
        }
        is SettingsDialogState.ClearCookies -> {
            ClearCookiesDialog(
                onClearCookiesConfirmed,
                onDismissalRequested,
            )
        }
        null -> Unit
    }
}

@Composable
private fun ChangeTitleDialog(
    initialValue: String,
    onConfirm: (String) -> Unit,
    onDismissalRequested: () -> Unit,
) {
    TextInputDialog(
        title = stringResource(R.string.title_set_title),
        initialValue = initialValue,
        transformValue = {
            it.take(50)
        },
        onDismissRequest = { text ->
            if (text != null) {
                onConfirm(text)
            } else {
                onDismissalRequested()
            }
        },
    )
}

@Composable
private fun LockAppDialog(
    onConfirm: (String) -> Unit,
    onDismissalRequested: () -> Unit,
) {
    TextInputDialog(
        title = stringResource(R.string.dialog_title_lock_app),
        message = stringResource(R.string.dialog_text_lock_app),
        confirmButton = stringResource(R.string.button_lock_app),
        allowEmpty = false,
        transformValue = {
            it.take(50)
        },
        dismissButton = {
            TextButton(
                onClick = onDismissalRequested,
            ) {
                Text(stringResource(R.string.dialog_cancel))
            }
        },
        onDismissRequest = { text ->
            if (text != null) {
                onConfirm(text)
            } else {
                onDismissalRequested()
            }
        },
    )
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

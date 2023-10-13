package ch.rmy.android.http_shortcuts.activities.settings

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.ChangeTitleDialog
import ch.rmy.android.http_shortcuts.components.Checkbox
import ch.rmy.android.http_shortcuts.components.ConfirmDialog
import ch.rmy.android.http_shortcuts.components.TextInputDialog
import ch.rmy.android.http_shortcuts.utils.Validation

@Composable
fun SettingsDialogs(
    dialogState: SettingsDialogState?,
    onLockConfirmed: (String, Boolean) -> Unit,
    onTitleChangeConfirmed: (String) -> Unit,
    onUserAgentChangeConfirmed: (String) -> Unit,
    onClearCookiesConfirmed: () -> Unit,
    onDismissalRequested: () -> Unit,
) {
    when (dialogState) {
        is SettingsDialogState.ChangeTitle -> {
            ChangeTitleDialog(
                initialValue = dialogState.oldTitle,
                onConfirm = onTitleChangeConfirmed,
                onDismissalRequested = onDismissalRequested,
            )
        }
        is SettingsDialogState.ChangeUserAgent -> {
            ChangeUserAgentDialog(
                initialValue = dialogState.oldUserAgent,
                onConfirm = onUserAgentChangeConfirmed,
                onDismissalRequested = onDismissalRequested,
            )
        }
        is SettingsDialogState.LockApp -> {
            LockAppDialog(
                canUseBiometrics = dialogState.canUseBiometrics,
                onConfirm = onLockConfirmed,
                onDismissalRequested = onDismissalRequested,
            )
        }
        is SettingsDialogState.ClearCookies -> {
            ClearCookiesDialog(
                onConfirm = onClearCookiesConfirmed,
                onDismissalRequested = onDismissalRequested,
            )
        }
        null -> Unit
    }
}

@Composable
private fun ChangeUserAgentDialog(
    initialValue: String,
    onConfirm: (String) -> Unit,
    onDismissalRequested: () -> Unit,
) {
    TextInputDialog(
        title = stringResource(R.string.title_set_user_agent),
        message = stringResource(R.string.instructions_set_user_agent),
        initialValue = initialValue,
        transformValue = {
            it.filter(Validation::isValidInHeaderValue).take(300)
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
    canUseBiometrics: Boolean,
    onConfirm: (password: String, useBiometrics: Boolean) -> Unit,
    onDismissalRequested: () -> Unit,
) {
    var useBiometrics by remember {
        mutableStateOf(canUseBiometrics)
    }

    TextInputDialog(
        title = stringResource(R.string.dialog_title_lock_app),
        message = stringResource(R.string.dialog_text_lock_app),
        confirmButton = stringResource(R.string.button_lock_app),
        allowEmpty = false,
        monospace = true,
        singleLine = true,
        transformValue = {
            it.take(50)
        },
        bottomContent = {
            if (canUseBiometrics) {
                Checkbox(
                    label = stringResource(R.string.label_app_lock_use_biometrics),
                    checked = useBiometrics,
                    onCheckedChange = {
                        useBiometrics = it
                    },
                )
            }
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
                onConfirm(text, useBiometrics)
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

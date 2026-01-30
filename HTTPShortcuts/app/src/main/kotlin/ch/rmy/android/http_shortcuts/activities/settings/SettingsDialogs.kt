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
import ch.rmy.android.http_shortcuts.components.ProgressDialog
import ch.rmy.android.http_shortcuts.components.TextInputDialog
import ch.rmy.android.http_shortcuts.components.UnlockAppDialog
import ch.rmy.android.http_shortcuts.utils.Validation

@Composable
fun SettingsDialogs(
    dialogState: SettingsDialogState?,
    onLockConfirmed: (String, Boolean) -> Unit,
    onLockRemoved: () -> Unit,
    onUnlockDialogSubmitted: (String) -> Unit,
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
                placeholder = dialogState.placeholder,
                onConfirm = onUserAgentChangeConfirmed,
                onDismissalRequested = onDismissalRequested,
            )
        }
        is SettingsDialogState.LockApp -> {
            LockAppDialog(
                canUseBiometrics = dialogState.canUseBiometrics,
                hasLock = dialogState.hasLock,
                usesBiometrics = dialogState.usesBiometrics,
                onConfirm = onLockConfirmed,
                onRemove = onLockRemoved,
                onDismissalRequested = onDismissalRequested,
            )
        }
        is SettingsDialogState.Unlock -> {
            UnlockAppDialog(
                tryAgain = dialogState.tryAgain,
                onSubmitted = onUnlockDialogSubmitted,
                onDismissed = onDismissalRequested,
            )
        }
        is SettingsDialogState.ClearCookies -> {
            ClearCookiesDialog(
                onConfirm = onClearCookiesConfirmed,
                onDismissalRequested = onDismissalRequested,
            )
        }
        is SettingsDialogState.Progress -> {
            ProgressDialog(
                onDismissRequest = {},
            )
        }
        null -> Unit
    }
}

@Composable
private fun ChangeUserAgentDialog(
    initialValue: String,
    placeholder: String,
    onConfirm: (String) -> Unit,
    onDismissalRequested: () -> Unit,
) {
    TextInputDialog(
        title = stringResource(R.string.title_set_user_agent),
        message = stringResource(R.string.instructions_set_user_agent),
        initialValue = initialValue,
        placeholder = placeholder,
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
    hasLock: Boolean,
    usesBiometrics: Boolean,
    onConfirm: (password: String, useBiometrics: Boolean) -> Unit,
    onRemove: () -> Unit,
    onDismissalRequested: () -> Unit,
) {
    var useBiometrics by remember {
        mutableStateOf(canUseBiometrics && usesBiometrics)
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
        dismissButton = if (hasLock) {
            {
                TextButton(
                    onClick = onRemove,
                ) {
                    Text(stringResource(R.string.dialog_remove))
                }
            }
        } else {
            null
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

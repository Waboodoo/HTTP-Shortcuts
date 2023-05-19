package ch.rmy.android.http_shortcuts.activities.editor.authentication

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.SelectDialog
import ch.rmy.android.http_shortcuts.components.SelectDialogEntry
import ch.rmy.android.http_shortcuts.components.TextInputDialog

@Composable
fun AuthenticationDialogs(
    dialogState: AuthenticationDialogState?,
    onFromSystemOptionSelected: () -> Unit,
    onFromFileOptionSelected: () -> Unit,
    onPasswordConfirmed: (String) -> Unit,
    onDismissed: () -> Unit,
) {
    when (dialogState) {
        is AuthenticationDialogState.SelectClientCertType -> {
            SelectClientCertTypeDialog(
                onFromSystemOptionSelected = onFromSystemOptionSelected,
                onFromFileOptionSelected = onFromFileOptionSelected,
                onDismissed = onDismissed,
            )
        }
        is AuthenticationDialogState.PasswordPromptForCertFile -> {
            PasswordPromptDialog(
                onConfirmed = onPasswordConfirmed,
                onDismissed = onDismissed,
            )
        }
        null -> Unit
    }
}

@Composable
private fun SelectClientCertTypeDialog(
    onFromSystemOptionSelected: () -> Unit,
    onFromFileOptionSelected: () -> Unit,
    onDismissed: () -> Unit,
) {
    SelectDialog(
        title = stringResource(R.string.title_client_cert),
        onDismissRequest = onDismissed,
    ) {
        SelectDialogEntry(
            label = stringResource(R.string.label_client_cert_from_os),
            description = stringResource(R.string.label_client_cert_from_os_subtitle),
            onClick = onFromSystemOptionSelected,
        )
        SelectDialogEntry(
            label = stringResource(R.string.label_client_cert_from_file),
            description = stringResource(R.string.label_client_cert_from_file_subtitle),
            onClick = onFromFileOptionSelected,
        )
    }
}

@Composable
private fun PasswordPromptDialog(
    onConfirmed: (String) -> Unit,
    onDismissed: () -> Unit,
) {
    TextInputDialog(
        title = stringResource(R.string.title_client_cert_file_password),
        allowEmpty = false,
        onDismissRequest = {
            if (it != null) {
                onConfirmed(it)
            } else {
                onDismissed()
            }
        },
        keyboardType = KeyboardType.Password,
    )
}

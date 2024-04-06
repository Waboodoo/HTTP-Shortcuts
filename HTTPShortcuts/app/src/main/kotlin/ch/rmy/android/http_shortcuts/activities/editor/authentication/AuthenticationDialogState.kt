package ch.rmy.android.http_shortcuts.activities.editor.authentication

import androidx.compose.runtime.Stable

@Stable
sealed class AuthenticationDialogState {
    @Stable
    data object SelectClientCertType : AuthenticationDialogState()

    @Stable
    data class PasswordPromptForCertFile(val fileName: String) : AuthenticationDialogState()
}

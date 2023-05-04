package ch.rmy.android.http_shortcuts.activities.editor.authentication

import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.data.enums.ClientCertParams
import ch.rmy.android.http_shortcuts.data.enums.ShortcutAuthenticationType

@Stable
data class AuthenticationViewState(
    val dialogState: AuthenticationDialogState? = null,
    val authenticationType: ShortcutAuthenticationType = ShortcutAuthenticationType.NONE,
    val username: String = "",
    val password: String = "",
    val token: String = "",
    val clientCertParams: ClientCertParams? = null,
    val isClientCertButtonEnabled: Boolean = true,
)

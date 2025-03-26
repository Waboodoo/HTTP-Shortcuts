package ch.rmy.android.http_shortcuts.activities.editor.authentication

import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.data.enums.ClientCertParams
import ch.rmy.android.http_shortcuts.data.enums.ShortcutAuthenticationType
import ch.rmy.android.http_shortcuts.data.enums.ShortcutExecutionType

@Stable
data class AuthenticationViewState(
    val shortcutExecutionType: ShortcutExecutionType,
    val dialogState: AuthenticationDialogState? = null,
    val authenticationType: ShortcutAuthenticationType? = null,
    val username: String = "",
    val password: String = "",
    val token: String = "",
    val clientCertParams: ClientCertParams? = null,
    val isClientCertButtonEnabled: Boolean = true,
)

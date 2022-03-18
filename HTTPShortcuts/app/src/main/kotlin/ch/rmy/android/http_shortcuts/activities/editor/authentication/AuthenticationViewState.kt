package ch.rmy.android.http_shortcuts.activities.editor.authentication

import ch.rmy.android.http_shortcuts.data.enums.ShortcutAuthenticationType
import ch.rmy.android.http_shortcuts.data.models.VariableModel

data class AuthenticationViewState(
    val authenticationType: ShortcutAuthenticationType = ShortcutAuthenticationType.NONE,
    val username: String = "",
    val password: String = "",
    val token: String = "",
    val variables: List<VariableModel>? = null,
) {
    val isUsernameAndPasswordVisible
        get() = authenticationType.usesUsernameAndPassword

    val isTokenVisible
        get() = authenticationType.usesToken
}

package ch.rmy.android.http_shortcuts.activities.editor.authentication

import ch.rmy.android.http_shortcuts.data.models.Variable

data class AuthenticationViewState(
    val authenticationMethod: String = "",
    val isUsernameAndPasswordVisible: Boolean = false,
    val isTokenVisible: Boolean = false,
    val username: String = "",
    val password: String = "",
    val token: String = "",
    val variables: List<Variable>? = null,
)

package ch.rmy.android.http_shortcuts.activities.editor.authentication

import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.framework.utils.localization.StringResLocalizable
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.data.enums.ClientCertParams
import ch.rmy.android.http_shortcuts.data.enums.ShortcutAuthenticationType

data class AuthenticationViewState(
    val dialogState: DialogState? = null,
    val authenticationType: ShortcutAuthenticationType = ShortcutAuthenticationType.NONE,
    val username: String = "",
    val password: String = "",
    val token: String = "",
    val clientCertParams: ClientCertParams? = null,
    val isClientCertButtonEnabled: Boolean = true,
) {
    val isUsernameAndPasswordVisible
        get() = authenticationType.usesUsernameAndPassword

    val isTokenVisible
        get() = authenticationType.usesToken

    val clientCertSubtitle: Localizable
        get() = when (clientCertParams) {
            is ClientCertParams.Alias -> StringResLocalizable(R.string.label_subtitle_client_cert_in_use, clientCertParams.alias)
            is ClientCertParams.File -> StringResLocalizable(R.string.label_subtitle_client_cert_file_in_use)
            else -> StringResLocalizable(R.string.label_subtitle_no_client_cert)
        }
}

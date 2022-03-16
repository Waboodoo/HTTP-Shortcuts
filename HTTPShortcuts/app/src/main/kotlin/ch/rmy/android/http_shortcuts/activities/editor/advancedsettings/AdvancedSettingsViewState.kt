package ch.rmy.android.http_shortcuts.activities.editor.advancedsettings

import ch.rmy.android.framework.utils.localization.DurationLocalizable
import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.framework.utils.localization.StringResLocalizable
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.data.models.ClientCertParams
import ch.rmy.android.http_shortcuts.data.models.Variable
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

data class AdvancedSettingsViewState(
    val dialogState: DialogState? = null,
    val variables: List<Variable>? = null,
    val followRedirects: Boolean = false,
    val acceptAllCertificates: Boolean = false,
    val acceptCookies: Boolean = false,
    val clientCertParams: ClientCertParams? = null,
    val timeout: Duration = 0.milliseconds,
    val proxyHost: String = "",
    val proxyPort: String = "",
    val wifiSsid: String = "",
) {
    val isClientCertButtonEnabled
        get() = !acceptAllCertificates

    val timeoutSubtitle: Localizable
        get() = DurationLocalizable(timeout)

    val clientCertSubtitle: Localizable
        get() = when (clientCertParams) {
            is ClientCertParams.Alias -> StringResLocalizable(R.string.label_subtitle_client_cert_in_use, clientCertParams.alias)
            is ClientCertParams.File -> StringResLocalizable(R.string.label_subtitle_client_cert_file_in_use)
            else -> StringResLocalizable(R.string.label_subtitle_no_client_cert)
        }
}

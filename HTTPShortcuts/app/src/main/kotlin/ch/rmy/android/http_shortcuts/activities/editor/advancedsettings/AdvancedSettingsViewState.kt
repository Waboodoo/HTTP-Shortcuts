package ch.rmy.android.http_shortcuts.activities.editor.advancedsettings

import ch.rmy.android.framework.utils.localization.DurationLocalizable
import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.data.models.VariableModel
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

data class AdvancedSettingsViewState(
    val dialogState: DialogState? = null,
    val variables: List<VariableModel>? = null,
    val followRedirects: Boolean = false,
    val acceptAllCertificates: Boolean = false,
    val acceptCookies: Boolean = false,
    val timeout: Duration = 0.milliseconds,
    val proxyHost: String = "",
    val proxyPort: String = "",
    val wifiSsid: String = "",
) {
    val timeoutSubtitle: Localizable
        get() = DurationLocalizable(timeout)
}

package ch.rmy.android.http_shortcuts.activities.editor.advancedsettings

import androidx.compose.runtime.Stable
import ch.rmy.android.framework.utils.localization.DurationLocalizable
import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.http_shortcuts.data.enums.ProxyType
import kotlin.time.Duration

@Stable
data class AdvancedSettingsViewState(
    val dialogState: AdvancedSettingsDialogState? = null,
    val followRedirects: Boolean,
    val acceptAllCertificates: Boolean,
    val acceptCookies: Boolean,
    val timeout: Duration,
    val proxyType: ProxyType?,
    val proxyHost: String,
    val proxyPort: String,
    val proxyUsername: String,
    val proxyPassword: String,
    val requireSpecificWifi: Boolean,
    val wifiSsid: String,
) {
    val timeoutSubtitle: Localizable
        get() = DurationLocalizable(timeout)
}

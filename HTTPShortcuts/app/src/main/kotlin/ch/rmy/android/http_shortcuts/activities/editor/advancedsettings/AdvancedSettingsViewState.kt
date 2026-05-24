package ch.rmy.android.http_shortcuts.activities.editor.advancedsettings

import androidx.compose.runtime.Stable
import ch.rmy.android.framework.utils.localization.DurationLocalizable
import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.http_shortcuts.activities.editor.advancedsettings.models.HostVerificationType
import ch.rmy.android.http_shortcuts.data.enums.IpVersion
import ch.rmy.android.http_shortcuts.data.enums.NetworkPreference
import ch.rmy.android.http_shortcuts.data.enums.ProxyType
import ch.rmy.android.http_shortcuts.data.enums.ShortcutExecutionType
import kotlin.time.Duration

@Stable
data class AdvancedSettingsViewState(
    val dialogState: AdvancedSettingsDialogState? = null,
    val shortcutExecutionType: ShortcutExecutionType,
    val followRedirects: Boolean,
    val hostVerificationEnabled: Boolean,
    val hostVerificationType: HostVerificationType,
    val certificateFingerprint: String,
    val acceptCookies: Boolean,
    val keepConnectionOpen: Boolean,
    val timeout: Duration,
    val ipVersion: IpVersion?,
    val proxyType: ProxyType?,
    val proxyHost: String,
    val proxyPort: String,
    val proxyUsername: String,
    val proxyPassword: String,
    val requireSpecificWifi: Boolean,
    val wifiSsid: String,
    val networkPreference: NetworkPreference?,
) {
    val timeoutSubtitle: Localizable
        get() = DurationLocalizable(timeout)
}

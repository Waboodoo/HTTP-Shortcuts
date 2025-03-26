package ch.rmy.android.http_shortcuts.activities.editor.basicsettings

import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.activities.editor.basicsettings.models.InstalledBrowser
import ch.rmy.android.http_shortcuts.data.dtos.TargetBrowser
import ch.rmy.android.http_shortcuts.data.enums.HttpMethod
import ch.rmy.android.http_shortcuts.data.enums.ShortcutExecutionType

@Stable
data class BasicRequestSettingsViewState(
    val shortcutExecutionType: ShortcutExecutionType,
    val method: HttpMethod = HttpMethod.GET,
    val url: String = "",
    val targetBrowser: TargetBrowser,
    val browserPackageNameOptions: List<InstalledBrowser> = emptyList(),
    var wolMacAddress: String = "",
    var wolPort: String = "9",
    var wolBroadcastAddress: String = "",
)

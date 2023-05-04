package ch.rmy.android.http_shortcuts.activities.editor.basicsettings

import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.activities.editor.basicsettings.models.InstalledBrowser

@Stable
data class BasicRequestSettingsViewState(
    val methodVisible: Boolean = false,
    val method: String = "GET",
    val url: String = "",
    val browserPackageName: String = "",
    val browserPackageNameVisible: Boolean = false,
    val browserPackageNameOptions: List<InstalledBrowser> = emptyList(),
)

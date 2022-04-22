package ch.rmy.android.http_shortcuts.activities.editor.basicsettings

import ch.rmy.android.http_shortcuts.activities.editor.basicsettings.models.InstalledBrowser
import ch.rmy.android.http_shortcuts.data.models.VariableModel

data class BasicRequestSettingsViewState(
    val methodVisible: Boolean = false,
    val method: String = "GET",
    val url: String = "",
    val variables: List<VariableModel>? = null,
    val browserPackageName: String = "",
    val browserPackageNameVisible: Boolean = false,
    val browserPackageNameOptions: List<InstalledBrowser> = emptyList(),
)

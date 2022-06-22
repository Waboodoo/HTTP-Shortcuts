package ch.rmy.android.http_shortcuts.activities.editor.basicsettings

import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.activities.editor.basicsettings.models.InstalledBrowser

data class BasicRequestSettingsViewState(
    val dialogState: DialogState? = null,
    val methodVisible: Boolean = false,
    val method: String = "GET",
    val url: String = "",
    val browserPackageName: String = "",
    val browserPackageNameVisible: Boolean = false,
    val browserPackageNameOptions: List<InstalledBrowser> = emptyList(),
)

package ch.rmy.android.http_shortcuts.activities.editor.basicsettings

import ch.rmy.android.http_shortcuts.data.models.Variable

data class BasicRequestSettingsViewState(
    val methodVisible: Boolean = false,
    val method: String = "GET",
    val url: String = "",
    val variables: List<Variable>? = null,
    val browserPackageName: String = "",
    val browserPackageNameVisible: Boolean = false,
)

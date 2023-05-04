package ch.rmy.android.http_shortcuts.activities.editor.basicsettings.models

import androidx.compose.runtime.Stable

@Stable
data class InstalledBrowser(
    val packageName: String,
    val appName: String? = null,
)

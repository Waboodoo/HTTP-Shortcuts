package ch.rmy.android.http_shortcuts.activities.about

import androidx.compose.runtime.Stable

@Stable
data class AboutViewState(
    val versionNumber: String,
    val fDroidVisible: Boolean,
    val changeLogDialogPermanentlyHidden: Boolean,
    val changeLogDialogVisible: Boolean = false,
)

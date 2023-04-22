package ch.rmy.android.http_shortcuts.activities.globalcode

import androidx.compose.runtime.Stable

@Stable
data class GlobalScriptingViewState(
    val dialogState: GlobalScriptingDialogState? = null,
    val globalCode: String = "",
    val hasChanges: Boolean = false,
) {
    val saveButtonEnabled
        get() = hasChanges
}

package ch.rmy.android.http_shortcuts.activities.globalcode

import androidx.compose.runtime.Stable

@Stable
sealed class GlobalScriptingDialogState {
    data object DiscardWarning : GlobalScriptingDialogState()
}

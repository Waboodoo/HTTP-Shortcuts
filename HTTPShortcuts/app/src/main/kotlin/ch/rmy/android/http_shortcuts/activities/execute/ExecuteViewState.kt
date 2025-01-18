package ch.rmy.android.http_shortcuts.activities.execute

import androidx.compose.runtime.Stable

@Stable
data class ExecuteViewState(
    val dialogState: ExecuteDialogState<*>? = null,
    val executionInProgress: Boolean = false,
) {
    val progressSpinnerVisible: Boolean
        get() = executionInProgress && dialogState == null
}

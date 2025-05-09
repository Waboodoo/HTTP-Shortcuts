package ch.rmy.android.http_shortcuts.activities.variables.editor

import androidx.compose.runtime.Stable

@Stable
sealed class GlobalVariableEditorDialogState {
    @Stable
    data object DiscardWarning : GlobalVariableEditorDialogState()
}

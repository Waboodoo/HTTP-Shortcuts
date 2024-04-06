package ch.rmy.android.http_shortcuts.activities.variables.editor

import androidx.compose.runtime.Stable

@Stable
sealed class VariableEditorDialogState {
    @Stable
    data object DiscardWarning : VariableEditorDialogState()
}

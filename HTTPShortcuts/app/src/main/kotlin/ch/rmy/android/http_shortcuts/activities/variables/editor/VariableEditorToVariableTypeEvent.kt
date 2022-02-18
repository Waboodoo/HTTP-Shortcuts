package ch.rmy.android.http_shortcuts.activities.variables.editor

sealed interface VariableEditorToVariableTypeEvent {
    object Validate : VariableEditorToVariableTypeEvent
}

package ch.rmy.android.http_shortcuts.activities.variables.editor

sealed interface VariableTypeToVariableEditorEvent {
    object Initialized : VariableTypeToVariableEditorEvent
    data class Validated(val valid: Boolean) : VariableTypeToVariableEditorEvent
}

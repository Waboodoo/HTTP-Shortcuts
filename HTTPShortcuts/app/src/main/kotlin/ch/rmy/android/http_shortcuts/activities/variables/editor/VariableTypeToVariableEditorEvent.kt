package ch.rmy.android.http_shortcuts.activities.variables.editor

sealed interface VariableTypeToVariableEditorEvent {
    data class Validated(val valid: Boolean) : VariableTypeToVariableEditorEvent
}

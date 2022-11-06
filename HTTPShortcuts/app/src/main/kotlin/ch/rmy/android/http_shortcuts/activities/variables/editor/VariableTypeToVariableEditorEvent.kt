package ch.rmy.android.http_shortcuts.activities.variables.editor

import ch.rmy.android.framework.viewmodel.ViewModelEvent

abstract class VariableTypeToVariableEditorEvent : ViewModelEvent() {
    data class Validated(val valid: Boolean) : VariableTypeToVariableEditorEvent()
}

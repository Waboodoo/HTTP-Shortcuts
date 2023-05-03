package ch.rmy.android.http_shortcuts.activities.variables.editor

import ch.rmy.android.framework.viewmodel.ViewModelEvent

abstract class VariableEditorEvent : ViewModelEvent() {
    object FocusVariableKeyInput : VariableEditorEvent()
}

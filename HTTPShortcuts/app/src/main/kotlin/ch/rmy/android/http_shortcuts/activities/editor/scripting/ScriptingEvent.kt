package ch.rmy.android.http_shortcuts.activities.editor.scripting

import ch.rmy.android.framework.viewmodel.ViewModelEvent

abstract class ScriptingEvent : ViewModelEvent() {
    data class InsertCodeSnippet(
        val textBeforeCursor: String,
        val textAfterCursor: String,
    ) : ScriptingEvent()
}

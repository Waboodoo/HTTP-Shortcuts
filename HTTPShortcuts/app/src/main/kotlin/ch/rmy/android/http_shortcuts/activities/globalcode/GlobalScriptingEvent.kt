package ch.rmy.android.http_shortcuts.activities.globalcode

import ch.rmy.android.framework.viewmodel.ViewModelEvent

abstract class GlobalScriptingEvent : ViewModelEvent() {
    object ShowCodeSnippetPicker : GlobalScriptingEvent()
    data class InsertCodeSnippet(
        val textBeforeCursor: String,
        val textAfterCursor: String,
    ) : GlobalScriptingEvent()
}

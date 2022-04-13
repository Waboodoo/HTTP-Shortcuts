package ch.rmy.android.http_shortcuts.activities.editor.scripting

import ch.rmy.android.framework.viewmodel.ViewModelEvent

abstract class ScriptingEvent : ViewModelEvent() {
    data class ShowCodeSnippetPicker(
        val includeFileOptions: Boolean,
        val includeResponseOptions: Boolean,
        val includeNetworkErrorOption: Boolean,
        val target: TargetCodeFieldType,
    ) : ScriptingEvent()

    data class InsertCodeSnippet(
        val target: TargetCodeFieldType,
        val textBeforeCursor: String,
        val textAfterCursor: String,
    ) : ScriptingEvent()
}

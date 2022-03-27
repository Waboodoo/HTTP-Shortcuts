package ch.rmy.android.http_shortcuts.activities.editor.scripting

import ch.rmy.android.framework.viewmodel.ViewModelEvent
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon

abstract class ScriptingEvent : ViewModelEvent() {
    data class ShowCodeSnippetPicker(
        val includeFileOptions: Boolean,
        val includeResponseOptions: Boolean,
        val includeNetworkErrorOption: Boolean,
        val target: Target,
    ) : ScriptingEvent() {
        enum class Target {
            PREPARE,
            SUCCESS,
            FAILURE,
        }
    }
    object OpenCustomIconPicker : ScriptingEvent()
    object OpenIpackIconPicker : ScriptingEvent()
    data class InsertChangeIconSnippet(
        val shortcutPlaceholder: String,
        val icon: ShortcutIcon,
    ) : ScriptingEvent()
}

package ch.rmy.android.http_shortcuts.activities.settings.globalcode

import ch.rmy.android.framework.viewmodel.ViewModelEvent
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon

abstract class GlobalScriptingEvent : ViewModelEvent() {
    object ShowCodeSnippetPicker : GlobalScriptingEvent()
    object OpenCustomIconPicker : GlobalScriptingEvent()
    object OpenIpackIconPicker : GlobalScriptingEvent()
    data class InsertChangeIconSnippet(
        val shortcutPlaceholder: String,
        val icon: ShortcutIcon,
    ) : GlobalScriptingEvent()
}

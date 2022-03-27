package ch.rmy.android.http_shortcuts.activities.editor

import ch.rmy.android.framework.viewmodel.ViewModelEvent

abstract class ShortcutEditorEvent : ViewModelEvent() {
    object OpenCustomIconPicker : ShortcutEditorEvent()
    object OpenIpackIconPicker : ShortcutEditorEvent()
    object FocusNameInputField : ShortcutEditorEvent()
}

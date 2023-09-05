package ch.rmy.android.http_shortcuts.activities.main

import ch.rmy.android.framework.viewmodel.ViewModelEvent
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.dtos.ShortcutPlaceholder

abstract class ShortcutListEvent : ViewModelEvent() {
    object OpenFilePickerForExport : ViewModelEvent()

    data class PlaceShortcutOnHomeScreen(val shortcut: ShortcutPlaceholder) : ShortcutListEvent()

    data class RemoveShortcutFromHomeScreen(val shortcut: ShortcutPlaceholder) : ShortcutListEvent()

    data class SelectShortcut(val shortcutId: ShortcutId) : ShortcutListEvent()
}

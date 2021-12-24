package ch.rmy.android.http_shortcuts.activities.main

import ch.rmy.android.http_shortcuts.data.dtos.LauncherShortcut

sealed interface ChildViewModelEvent {
    data class MovingModeChanged(val enabled: Boolean) : ChildViewModelEvent

    object ShortcutEdited : ChildViewModelEvent

    data class PlaceShortcutOnHomeScreen(val shortcut: LauncherShortcut) : ChildViewModelEvent

    data class RemoveShortcutFromHomeScreen(val shortcut: LauncherShortcut) : ChildViewModelEvent

    data class SelectShortcut(val shortcutId: String) : ChildViewModelEvent
}

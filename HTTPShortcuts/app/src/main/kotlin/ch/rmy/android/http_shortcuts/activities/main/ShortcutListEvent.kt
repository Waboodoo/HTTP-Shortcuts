package ch.rmy.android.http_shortcuts.activities.main

import ch.rmy.android.framework.viewmodel.ViewModelEvent
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.dtos.ShortcutPlaceholder

abstract class ShortcutListEvent : ViewModelEvent() {
    data class OpenShortcutEditor(val shortcutId: ShortcutId, val categoryId: CategoryId) : ViewModelEvent()

    object OpenFilePickerForExport : ViewModelEvent()

    object ShortcutEdited : ShortcutListEvent()

    data class PlaceShortcutOnHomeScreen(val shortcut: ShortcutPlaceholder) : ShortcutListEvent()

    data class RemoveShortcutFromHomeScreen(val shortcut: ShortcutPlaceholder) : ShortcutListEvent()

    data class SelectShortcut(val shortcutId: ShortcutId) : ShortcutListEvent()
}

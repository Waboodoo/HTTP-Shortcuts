package ch.rmy.android.http_shortcuts.activities.main

import ch.rmy.android.framework.viewmodel.ViewModelEvent
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.dtos.LauncherShortcut
import ch.rmy.android.http_shortcuts.import_export.ExportFormat

abstract class ShortcutListEvent : ViewModelEvent() {
    data class OpenShortcutEditor(val shortcutId: ShortcutId, val categoryId: CategoryId) : ViewModelEvent()

    data class OpenFilePickerForExport(val exportFormat: ExportFormat) : ViewModelEvent()

    data class MovingModeChanged(val enabled: Boolean) : ShortcutListEvent()

    object ShortcutEdited : ShortcutListEvent()

    data class PlaceShortcutOnHomeScreen(val shortcut: LauncherShortcut) : ShortcutListEvent()

    data class RemoveShortcutFromHomeScreen(val shortcut: LauncherShortcut) : ShortcutListEvent()

    data class SelectShortcut(val shortcutId: ShortcutId) : ShortcutListEvent()
}

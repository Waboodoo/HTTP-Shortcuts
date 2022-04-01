package ch.rmy.android.http_shortcuts.activities.main

import ch.rmy.android.framework.viewmodel.ViewModelEvent
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.import_export.ExportFormat

abstract class ShortcutListEvent : ViewModelEvent() {
    data class OpenShortcutEditor(val shortcutId: ShortcutId, val categoryId: CategoryId) : ViewModelEvent()
    data class OpenFilePickerForExport(val exportFormat: ExportFormat) : ViewModelEvent()
}

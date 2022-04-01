package ch.rmy.android.http_shortcuts.activities.main

import ch.rmy.android.framework.viewmodel.ViewModelEvent
import ch.rmy.android.http_shortcuts.import_export.ExportFormat

abstract class ShortcutListEvent : ViewModelEvent() {
    data class OpenShortcutEditor(val shortcutId: String, val categoryId: String) : ViewModelEvent()
    data class OpenFilePickerForExport(val exportFormat: ExportFormat) : ViewModelEvent()
}

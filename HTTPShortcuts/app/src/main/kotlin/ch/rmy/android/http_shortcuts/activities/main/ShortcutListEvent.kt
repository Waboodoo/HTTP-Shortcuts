package ch.rmy.android.http_shortcuts.activities.main

import android.net.Uri
import ch.rmy.android.framework.viewmodel.ViewModelEvent
import ch.rmy.android.http_shortcuts.import_export.ExportFormat

abstract class ShortcutListEvent : ViewModelEvent() {
    data class ShowFileExportDialog(val shortcutId: String, val format: ExportFormat, val variableIds: Collection<String>) : ViewModelEvent()

    data class StartExport(val shortcutId: String, val uri: Uri, val format: ExportFormat, val variableIds: Collection<String>) : ViewModelEvent()
}

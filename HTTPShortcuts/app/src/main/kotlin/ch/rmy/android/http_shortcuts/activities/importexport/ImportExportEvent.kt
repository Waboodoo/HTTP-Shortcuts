package ch.rmy.android.http_shortcuts.activities.importexport

import ch.rmy.android.framework.viewmodel.ViewModelEvent
import ch.rmy.android.http_shortcuts.import_export.ExportFormat

abstract class ImportExportEvent : ViewModelEvent() {
    data class OpenFilePickerForExport(val exportFormat: ExportFormat) : ImportExportEvent()

    object OpenFilePickerForImport : ImportExportEvent()
}

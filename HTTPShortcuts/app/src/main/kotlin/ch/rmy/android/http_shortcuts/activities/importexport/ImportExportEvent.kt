package ch.rmy.android.http_shortcuts.activities.importexport

import ch.rmy.android.framework.viewmodel.ViewModelEvent

abstract class ImportExportEvent : ViewModelEvent() {
    object OpenFilePickerForExport : ImportExportEvent()

    object OpenFilePickerForImport : ImportExportEvent()
}

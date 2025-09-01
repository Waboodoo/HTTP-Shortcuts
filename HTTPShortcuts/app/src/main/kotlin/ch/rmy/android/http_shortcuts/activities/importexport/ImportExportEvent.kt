package ch.rmy.android.http_shortcuts.activities.importexport

import android.net.Uri
import ch.rmy.android.framework.viewmodel.ViewModelEvent

abstract class ImportExportEvent : ViewModelEvent() {
    object OpenFilePickerForImport : ImportExportEvent()

    data class SendExport(val export: Uri) : ImportExportEvent()
}

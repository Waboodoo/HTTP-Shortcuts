package ch.rmy.android.http_shortcuts.activities.settings.importexport

import ch.rmy.android.framework.viewmodel.ViewModelEvent

abstract class ImportExportEvent : ViewModelEvent() {
    data class StartImportFromURL(val url: String) : ImportExportEvent()
}

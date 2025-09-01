package ch.rmy.android.http_shortcuts.activities.importexport

import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.import_export.Importer

@Stable
data class ImportExportViewState(
    val importStatus: Importer.ImportStatus? = null,
    val dialogState: ImportExportDialogState? = null,
)

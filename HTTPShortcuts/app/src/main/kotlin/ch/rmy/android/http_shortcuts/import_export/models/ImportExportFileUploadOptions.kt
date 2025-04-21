package ch.rmy.android.http_shortcuts.import_export.models

import ch.rmy.android.http_shortcuts.data.domains.working_directories.WorkingDirectoryId

data class ImportExportFileUploadOptions(
    val fileUploadType: String? = null,
    val directoryId: WorkingDirectoryId? = null,
    val fileName: String? = null,
    val value: String? = null,
    val useImageEditor: Boolean? = null,
)

typealias ImportFileUploadOptions = ImportExportFileUploadOptions

typealias ExportFileUploadOptions = ImportExportFileUploadOptions

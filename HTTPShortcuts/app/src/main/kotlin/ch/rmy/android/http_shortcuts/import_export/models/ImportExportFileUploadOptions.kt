package ch.rmy.android.http_shortcuts.import_export.models

data class ImportExportFileUploadOptions(
    val fileUploadType: String? = null,
    val file: String? = null,
    val useImageEditor: Boolean? = null,
)

typealias ImportFileUploadOptions = ImportExportFileUploadOptions

typealias ExportFileUploadOptions = ImportExportFileUploadOptions

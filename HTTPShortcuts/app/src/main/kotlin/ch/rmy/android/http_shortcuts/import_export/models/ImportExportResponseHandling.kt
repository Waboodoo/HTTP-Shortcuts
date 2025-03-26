package ch.rmy.android.http_shortcuts.import_export.models

import ch.rmy.android.http_shortcuts.data.domains.working_directories.WorkingDirectoryId

data class ImportExportResponseHandling(
    val actions: List<String>? = null,
    val uiType: String? = null,
    val successOutput: String? = null,
    val failureOutput: String? = null,
    val contentType: String? = null,
    val charset: String? = null,
    val successMessage: String? = null,
    val includeMetaInfo: Boolean? = null,
    val jsonArrayAsTable: Boolean? = null,
    val monospace: Boolean? = null,
    val fontSize: Int? = null,
    val javaScriptEnabled: Boolean? = null,
    val storeDirectoryId: WorkingDirectoryId? = null,
    val storeFileName: String? = null,
    val replaceFileIfExists: Boolean? = null,
)

typealias ImportResponseHandling = ImportExportResponseHandling

typealias ExportResponseHandling = ImportExportResponseHandling

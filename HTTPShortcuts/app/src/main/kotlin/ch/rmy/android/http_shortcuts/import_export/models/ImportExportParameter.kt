package ch.rmy.android.http_shortcuts.import_export.models

data class ImportExportParameter(
    val key: String? = null,
    val value: String? = null,
    val fileName: String? = null,
    val type: String? = null,
    val fileUploadOptions: ImportExportFileUploadOptions? = null,
) {
    fun validate() {
        require(key != null && key.isNotEmpty()) {
            "Parameter without a key found"
        }
    }
}

typealias ImportParameter = ImportExportParameter

typealias ExportParameter = ImportExportParameter

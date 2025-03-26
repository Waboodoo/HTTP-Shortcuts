package ch.rmy.android.http_shortcuts.import_export.models

import ch.rmy.android.http_shortcuts.utils.Validation

data class ImportExportHeader(
    val key: String? = null,
    val value: String? = null,
) {
    fun validate() {
        require(key != null && key.isNotEmpty()) {
            "Header without a key found"
        }
        require(key.none { !Validation.isValidInHeaderName(it) }) {
            "Invalid characters found in header name: $key"
        }
        require(value == null || value.none { !Validation.isValidInHeaderValue(it) }) {
            "Invalid characters found in header value: $value"
        }
    }
}

typealias ImportHeader = ImportExportHeader

typealias ExportHeader = ImportExportHeader

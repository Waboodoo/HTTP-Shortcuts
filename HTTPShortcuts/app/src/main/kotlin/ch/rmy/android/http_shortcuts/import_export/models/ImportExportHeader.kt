package ch.rmy.android.http_shortcuts.import_export.models

import androidx.annotation.Keep
import ch.rmy.android.http_shortcuts.utils.Validation

@Keep
data class ImportExportHeader(
    val key: String? = null,
    val value: String? = null,
) {
    fun validate() {
        require(!key.isNullOrEmpty()) {
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

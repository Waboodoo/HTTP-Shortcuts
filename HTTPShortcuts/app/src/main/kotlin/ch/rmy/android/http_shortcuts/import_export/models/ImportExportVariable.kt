package ch.rmy.android.http_shortcuts.import_export.models

import ch.rmy.android.framework.extensions.isInt
import ch.rmy.android.framework.extensions.isUUID
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableId
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableKey
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.variables.Variables

data class ImportExportVariable(
    val id: VariableId? = null,
    val key: VariableKey? = null,
    val type: String? = null,
    val value: String? = null,
    val data: String? = null,
    val rememberValue: Boolean? = null,
    val urlEncode: Boolean? = null,
    val jsonEncode: Boolean? = null,
    val title: String? = null,
    val message: String? = null,
    val isShareText: Boolean? = null,
    val isShareTitle: Boolean? = null,
    val isMultiline: Boolean? = null,
    val isExcludeValueFromExport: Boolean? = null,
) {
    fun validate() {
        require((id == null || id.isUUID() || id.isInt()) && id != Variable.TEMPORARY_ID) {
            "Invalid variable ID found, must be UUID: $id"
        }
        require(key != null && Variables.isValidVariableKey(key)) {
            "Invalid variable key: $key"
        }
    }
}

typealias ImportVariable = ImportExportVariable

typealias ExportVariable = ImportExportVariable

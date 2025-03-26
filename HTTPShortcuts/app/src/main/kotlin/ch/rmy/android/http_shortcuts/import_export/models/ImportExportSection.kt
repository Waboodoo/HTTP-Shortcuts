package ch.rmy.android.http_shortcuts.import_export.models

import ch.rmy.android.framework.extensions.isUUID
import ch.rmy.android.http_shortcuts.data.domains.sections.SectionId

data class ImportExportSection(
    val id: SectionId? = null,
    val name: String? = null,
) {
    fun validate() {
        require(id == null || id.isUUID()) { "Invalid section ID found, must be UUID: $id" }
        require(name != null && name.isNotBlank()) { "Section without a name found" }
    }
}

typealias ImportSection = ImportExportSection

typealias ExportSection = ImportExportSection

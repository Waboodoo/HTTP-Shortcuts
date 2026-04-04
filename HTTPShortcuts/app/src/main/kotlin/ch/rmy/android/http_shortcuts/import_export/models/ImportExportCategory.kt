package ch.rmy.android.http_shortcuts.import_export.models

import ch.rmy.android.framework.extensions.hasDuplicatesBy
import ch.rmy.android.framework.extensions.isInt
import ch.rmy.android.framework.extensions.isUUID
import ch.rmy.android.framework.utils.UUIDUtils.newUUID
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId

data class ImportExportCategory(
    val id: CategoryId?,
    val name: String? = null,
    val iconName: String? = null,
    val layoutType: String? = null,
    val background: String? = null,
    val hidden: Boolean? = null,
    val hiddenLabels: Boolean? = null,
    val scale: Float? = null,
    val shortcutClickBehavior: String? = null,
    val sections: List<ImportExportSection>? = null,
    val shortcuts: List<ImportExportShortcut>? = null,
) {
    fun validate() {
        require(id == null || id.isUUID() || id.isInt()) {
            "Invalid category ID found, must be UUID: $id"
        }
        require(!name.isNullOrBlank()) {
            "Category without a name found"
        }
        require(scale == null || (scale in 0.25f..10f)) {
            "Invalid category scale found"
        }
        shortcuts?.forEach { it.validate() }
        sections?.forEach { it.validate() }
        require(sections == null || !sections.hasDuplicatesBy { it.id ?: newUUID() }) { "Duplicate section IDs found" }
    }
}

typealias ImportCategory = ImportExportCategory

typealias ExportCategory = ImportExportCategory

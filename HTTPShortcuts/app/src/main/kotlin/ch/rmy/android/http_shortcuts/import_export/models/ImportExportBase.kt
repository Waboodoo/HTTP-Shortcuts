package ch.rmy.android.http_shortcuts.import_export.models

import ch.rmy.android.framework.extensions.hasDuplicatesBy
import ch.rmy.android.framework.utils.UUIDUtils.newUUID

data class ImportExportBase(
    val version: Long? = null,
    val compatibilityVersion: Long? = null,
    val categories: List<ImportExportCategory>? = null,
    val variables: List<ImportExportVariable>? = null,
    val certificatePins: List<ImportExportCertificatePin>? = null,
    val workingDirectories: List<ImportExportWorkingDirectory>? = null,
    val title: String? = null,
    val globalCode: String? = null,
) {
    fun validate() {
        require(version != null && version > 0L) {
            "Invalid file format, no valid version number found"
        }
        categories?.forEach { it.validate() }
        variables?.forEach { it.validate() }
        workingDirectories?.forEach { it.validate() }
        certificatePins?.forEach { it.validate() }
        require(categories == null || !categories.hasDuplicatesBy { it.id ?: newUUID() }) {
            "Duplicate category IDs"
        }
        require(variables == null || !variables.hasDuplicatesBy { it.id }) {
            "Duplicate variable IDs"
        }
        require(variables == null || !variables.hasDuplicatesBy { it.key }) {
            "Duplicate variable keys"
        }
        val sections = categories?.flatMap { it.sections ?: emptyList() }
        require(sections == null || !sections.hasDuplicatesBy { it.id ?: newUUID() }) {
            "Duplicate section IDs"
        }
        val shortcuts = categories?.flatMap { it.shortcuts ?: emptyList() }
        require(shortcuts == null || !shortcuts.hasDuplicatesBy { it.id ?: newUUID() }) {
            "Duplicate shortcut IDs"
        }
        require(categories?.all { c -> c.id != null || c.sections?.none { section -> section.id != null } != false } != false) {
            "A category without an ID must not contain sections with IDs"
        }
        require(categories?.all { c -> c.id != null || c.shortcuts?.none { shortcut -> shortcut.id != null } != false } != false) {
            "A category without an ID must not contain shortcuts with IDs"
        }
    }
}

typealias ImportBase = ImportExportBase

typealias ExportBase = ImportExportBase

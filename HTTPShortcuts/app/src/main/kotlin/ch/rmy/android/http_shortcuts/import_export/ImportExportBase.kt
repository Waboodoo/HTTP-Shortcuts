package ch.rmy.android.http_shortcuts.import_export

import ch.rmy.android.framework.extensions.hasDuplicatesBy
import ch.rmy.android.http_shortcuts.data.models.Category
import ch.rmy.android.http_shortcuts.data.models.CertificatePin
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.data.models.WorkingDirectory
import ch.rmy.android.http_shortcuts.extensions.isValidCertificateFingerprint

data class ImportExportBase(
    val version: Long,
    val compatibilityVersion: Long = 0,
    val categories: List<Category>,
    val variables: List<Variable>,
    val certificatePins: List<CertificatePin>,
    val workingDirectories: List<WorkingDirectory>,
    val title: String?,
    val globalCode: String?,
) {
    fun validate() {
        require(version > 0L) {
            "Invalid file format, no valid version number found"
        }
        categories.forEach(Category::validate)
        variables.forEach(Variable::validate)
        workingDirectories.forEach(WorkingDirectory::validate)
        certificatePins.forEach { it.validate() }
        require(!categories.hasDuplicatesBy { it.id }) {
            "Duplicate category IDs"
        }
        require(!variables.hasDuplicatesBy { it.id }) {
            "Duplicate variable IDs"
        }
        require(!variables.flatMap { it.options ?: emptyList() }.hasDuplicatesBy { it.id }) {
            "Duplicate variable option IDs"
        }
        require(!variables.hasDuplicatesBy { it.key }) {
            "Duplicate variable keys"
        }
        val shortcuts = categories.flatMap { it.shortcuts }
        require(!shortcuts.hasDuplicatesBy { it.id }) {
            "Duplicate shortcut IDs"
        }
        require(!shortcuts.flatMap { it.headers }.hasDuplicatesBy { it.id }) {
            "Duplicate header IDs"
        }
        require(!shortcuts.flatMap { it.parameters }.hasDuplicatesBy { it.id }) {
            "Duplicate parameter IDs"
        }
    }

    private fun CertificatePin.validate() {
        require(pattern.isNotEmpty()) {
            "Certificate pin without host pattern found"
        }
        require(hash.isValidCertificateFingerprint()) {
            "Invalid certificate fingerprint found: $hash"
        }
    }
}

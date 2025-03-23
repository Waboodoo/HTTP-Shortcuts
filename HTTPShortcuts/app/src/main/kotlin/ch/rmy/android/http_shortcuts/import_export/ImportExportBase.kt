package ch.rmy.android.http_shortcuts.import_export

import ch.rmy.android.framework.extensions.hasDuplicatesBy
import ch.rmy.android.framework.extensions.isInt
import ch.rmy.android.framework.extensions.isUUID
import ch.rmy.android.http_shortcuts.data.models.Category
import ch.rmy.android.http_shortcuts.data.models.CertificatePin
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.data.models.WorkingDirectory
import ch.rmy.android.http_shortcuts.extensions.isValidCertificateFingerprint
import ch.rmy.android.http_shortcuts.variables.Variables

data class ImportExportBase(
    val version: Long = 0,
    val compatibilityVersion: Long = 0,
    val categories: List<Category> = emptyList(),
    val variables: List<Variable> = emptyList(),
    val certificatePins: List<CertificatePin> = emptyList(),
    val workingDirectories: List<WorkingDirectory> = emptyList(),
    val title: String? = null,
    val globalCode: String? = null,
) {
    fun validate() {
        require(version > 0L) {
            "Invalid file format, no valid version number found"
        }
        categories.forEach(Category::validate)
        variables.forEach { it.validate() }
        workingDirectories.forEach { it.validate() }
        certificatePins.forEach { it.validate() }
        require(!categories.hasDuplicatesBy { it.id }) {
            "Duplicate category IDs"
        }
        require(!variables.hasDuplicatesBy { it.id }) {
            "Duplicate variable IDs"
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

    private fun Variable.validate() {
        require((id.isUUID() || id.isInt()) && id != Variable.TEMPORARY_ID) {
            "Invalid variable ID found, must be UUID: $id"
        }
        require(Variables.isValidVariableKey(key)) {
            "Invalid variable key: $key"
        }
    }

    private fun CertificatePin.validate() {
        require(id.isUUID()) {
            "Invalid certificate pin ID found, must be UUID: $id"
        }
        require(pattern.isNotEmpty()) {
            "Certificate pin without host pattern found"
        }
        require(hash.isValidCertificateFingerprint()) {
            "Invalid certificate fingerprint found: $hash"
        }
    }

    private fun WorkingDirectory.validate() {
        require(id.isUUID()) {
            "Invalid directory ID found, must be UUID: $id"
        }
        require(name.isNotEmpty()) {
            "Invalid directory name for working directory"
        }
        require(directory.scheme?.equals("content", ignoreCase = true) == true) {
            "Invalid directory URI for working directory"
        }
    }
}

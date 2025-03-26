package ch.rmy.android.http_shortcuts.import_export.models

import ch.rmy.android.framework.extensions.isUUID
import ch.rmy.android.http_shortcuts.data.domains.certificate_pins.CertificatePinId
import ch.rmy.android.http_shortcuts.extensions.isValidCertificateFingerprint

data class ImportExportCertificatePin(
    val id: CertificatePinId? = null,
    val pattern: String? = null,
    val hash: String? = null,
) {
    fun validate() {
        require(id == null || id.isUUID()) {
            "Invalid certificate pin ID found, must be UUID: $id"
        }
        require(pattern != null && pattern.isNotEmpty()) {
            "Certificate pin without host pattern found"
        }
        require(hash?.isValidCertificateFingerprint() == true) {
            "Invalid certificate fingerprint found: $hash"
        }
    }
}

typealias ImportCertificatePin = ImportExportCertificatePin

typealias ExportCertificatePin = ImportExportCertificatePin

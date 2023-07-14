package ch.rmy.android.http_shortcuts.data.models

import ch.rmy.android.framework.utils.UUIDUtils.newUUID
import ch.rmy.android.http_shortcuts.extensions.isValidCertificateFingerprint
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class CertificatePin() : RealmObject {

    constructor(pattern: String, hash: String) : this() {
        this.pattern = pattern
        this.hash = hash
    }

    @PrimaryKey
    var id: String = newUUID()
    var pattern: String = ""

    /**
     * Hex-encoded hash, either SHA-1 or SHA-256.
     */
    var hash: String = ""

    fun validate() {
        require(pattern.isNotEmpty()) {
            "Certificate pin without host pattern found"
        }
        require(hash.isValidCertificateFingerprint()) {
            "Invalid certificate fingerprint found: $hash"
        }
    }

    companion object {
        const val FIELD_ID = "id"
    }
}

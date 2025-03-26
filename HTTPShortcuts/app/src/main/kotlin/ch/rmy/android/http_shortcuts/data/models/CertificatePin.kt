package ch.rmy.android.http_shortcuts.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import ch.rmy.android.http_shortcuts.data.domains.certificate_pins.CertificatePinId

@Entity(tableName = "certificate_pin")
data class CertificatePin(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: CertificatePinId,
    @ColumnInfo(name = "pattern")
    val pattern: String,
    /**
     * Hex-encoded hash, either SHA-1 or SHA-256.
     */
    @ColumnInfo(name = "hash")
    val hash: String,
)

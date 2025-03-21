package ch.rmy.android.http_shortcuts.activities.certpinning.models

import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.data.domains.certificate_pins.CertificatePinId

@Stable
data class Pin(
    val id: CertificatePinId,
    val pattern: String,
    val hash: String,
) {
    @Stable
    fun formatted(): String =
        hash.chunked(2).joinToString(":")
}

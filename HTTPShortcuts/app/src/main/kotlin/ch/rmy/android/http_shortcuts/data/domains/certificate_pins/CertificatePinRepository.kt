package ch.rmy.android.http_shortcuts.data.domains.certificate_pins

import ch.rmy.android.http_shortcuts.data.Database
import ch.rmy.android.http_shortcuts.data.domains.BaseRepository
import ch.rmy.android.http_shortcuts.data.models.CertificatePin
import javax.inject.Inject

class CertificatePinRepository
@Inject
constructor(
    database: Database,
) : BaseRepository(database) {
    suspend fun getCertificatePins(): List<CertificatePin> = query {
        certificatePinDao().getCertificatePins()
    }
}

package ch.rmy.android.http_shortcuts.data.domains.certificate_pins

import ch.rmy.android.framework.utils.UUIDUtils
import ch.rmy.android.http_shortcuts.data.Database
import ch.rmy.android.http_shortcuts.data.domains.BaseRepository
import ch.rmy.android.http_shortcuts.data.models.CertificatePin
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class CertificatePinRepository
@Inject
constructor(
    database: Database,
) : BaseRepository(database) {
    suspend fun getCertificatePins(): List<CertificatePin> = query {
        certificatePinDao().getCertificatePins()
    }

    fun observeCertificatePins(): Flow<List<CertificatePin>> =
        queryFlow {
            certificatePinDao()
                .observeCertificatePins()
                .distinctUntilChanged()
                .map { certificatePins ->
                    certificatePins.map { certificatePin ->
                        CertificatePin(
                            id = certificatePin.id,
                            pattern = certificatePin.pattern,
                            hash = certificatePin.hash,
                        )
                    }
                }
        }

    suspend fun createCertificatePin(pattern: String, hash: String) = query {
        certificatePinDao()
            .insertCertificatePin(
                CertificatePin(
                    id = UUIDUtils.newUUID(),
                    pattern = pattern,
                    hash = hash,
                ),
            )
    }

    suspend fun updateCertificatePin(id: CertificatePinId, pattern: String, hash: String) = query {
        certificatePinDao()
            .insertCertificatePin(
                CertificatePin(
                    id = id,
                    pattern = pattern,
                    hash = hash,
                ),
            )
    }

    suspend fun deleteCertificatePinning(id: CertificatePinId) = query {
        certificatePinDao().deleteCertificatePin(id)
    }
}

package ch.rmy.android.http_shortcuts.data.domains.certificate_pins

import ch.rmy.android.framework.data.BaseRepository
import ch.rmy.android.framework.utils.UUIDUtils
import ch.rmy.android.http_shortcuts.data.Database
import ch.rmy.android.http_shortcuts.data.models.CertificatePin
import ch.rmy.android.http_shortcuts.import_export.Importer
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class CertificatePinRepository
@Inject
constructor(
    database: Database,
) : BaseRepository(database) {
    suspend fun getCertificatePins(): List<CertificatePin> =
        get(Database::certificatePinDao).get()

    fun getObservableCertificatePins(): Flow<List<CertificatePin>> =
        flow(Database::certificatePinDao) {
            observe()
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

    suspend fun createCertificatePin(pattern: String, hash: String) {
        get(Database::certificatePinDao)
            .insert(
                CertificatePin(
                    id = UUIDUtils.newUUID(),
                    pattern = pattern,
                    hash = hash,
                ),
            )
    }

    suspend fun updateCertificatePin(id: CertificatePinId, pattern: String, hash: String) {
        get(Database::certificatePinDao).insert(
            CertificatePin(
                id = id,
                pattern = pattern,
                hash = hash,
            ),
        )
    }

    suspend fun deleteCertificatePinning(id: CertificatePinId) {
        get(Database::certificatePinDao).delete(id)
    }

    suspend fun importPins(pins: List<CertificatePin>, mode: Importer.ImportMode) {
        with(get(Database::certificatePinDao)) {
            when (mode) {
                Importer.ImportMode.MERGE -> insert(pins)
                Importer.ImportMode.REPLACE -> replace(pins)
            }
        }
    }
}

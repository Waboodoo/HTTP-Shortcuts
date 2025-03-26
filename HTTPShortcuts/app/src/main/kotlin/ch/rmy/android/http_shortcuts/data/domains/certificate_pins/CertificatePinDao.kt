package ch.rmy.android.http_shortcuts.data.domains.certificate_pins

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ch.rmy.android.http_shortcuts.data.models.CertificatePin
import kotlinx.coroutines.flow.Flow

@Dao
interface CertificatePinDao {
    @Query("SELECT * FROM certificate_pin ORDER BY pattern ASC")
    suspend fun getCertificatePins(): List<CertificatePin>

    @Query("SELECT * FROM certificate_pin ORDER BY pattern ASC")
    fun observeCertificatePins(): Flow<List<CertificatePin>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCertificatePin(certificatePin: CertificatePin)

    @Query("DELETE FROM certificate_pin WHERE id = :id")
    suspend fun deleteCertificatePin(id: CertificatePinId)

    @Query("DELETE FROM certificate_pin")
    suspend fun deleteAllCertificatePins()
}

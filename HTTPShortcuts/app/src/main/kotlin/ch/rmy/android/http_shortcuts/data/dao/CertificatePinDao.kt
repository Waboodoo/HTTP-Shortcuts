package ch.rmy.android.http_shortcuts.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import ch.rmy.android.http_shortcuts.data.domains.certificate_pins.CertificatePinId
import ch.rmy.android.http_shortcuts.data.models.CertificatePin
import kotlinx.coroutines.flow.Flow

@Dao
interface CertificatePinDao {
    @Query("SELECT * FROM certificate_pin ORDER BY pattern ASC")
    suspend fun get(): List<CertificatePin>

    @Query("SELECT * FROM certificate_pin ORDER BY pattern ASC")
    fun observe(): Flow<List<CertificatePin>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(certificatePin: CertificatePin)

    @Transaction
    suspend fun insert(certificatePins: List<CertificatePin>) {
        certificatePins.forEach { pin ->
            insert(pin)
        }
    }

    @Transaction
    suspend fun replace(certificatePins: List<CertificatePin>) {
        deleteAll()
        insert(certificatePins)
    }

    @Query("DELETE FROM certificate_pin WHERE id = :id")
    suspend fun delete(id: CertificatePinId)

    @Query("DELETE FROM certificate_pin")
    suspend fun deleteAll()
}

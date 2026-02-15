package ch.rmy.android.http_shortcuts.data.domains.sync

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ch.rmy.android.http_shortcuts.data.enums.SyncType
import ch.rmy.android.http_shortcuts.data.models.SyncConfig
import java.time.Instant
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(syncConfig: SyncConfig)

    @Query("SELECT * FROM sync_config WHERE sync_type = :syncType LIMIT 1")
    suspend fun getSyncConfig(syncType: SyncType): SyncConfig?

    @Query("SELECT * FROM sync_config WHERE sync_type = :syncType LIMIT 1")
    fun observeSyncConfig(syncType: SyncType): Flow<SyncConfig?>

    @Query("UPDATE sync_config SET last_succeeded = :lastSucceeded WHERE sync_type = :syncType")
    suspend fun setLastSucceeded(syncType: SyncType, lastSucceeded: Instant)

    @Query("UPDATE sync_config SET last_failed = :lastFailed WHERE sync_type = :syncType")
    suspend fun setLastFailed(syncType: SyncType, lastFailed: Instant)
}

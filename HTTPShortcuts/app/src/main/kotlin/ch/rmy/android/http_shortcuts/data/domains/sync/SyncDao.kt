package ch.rmy.android.http_shortcuts.data.domains.sync

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ch.rmy.android.http_shortcuts.data.enums.SyncType
import ch.rmy.android.http_shortcuts.data.models.SyncConfig

@Dao
interface SyncDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(syncConfig: SyncConfig)

    @Query("SELECT * FROM sync_config WHERE sync_type = :syncType LIMIT 1")
    suspend fun getSyncConfig(syncType: SyncType): SyncConfig?
}

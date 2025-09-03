package ch.rmy.android.http_shortcuts.data.domains.sync

import ch.rmy.android.http_shortcuts.data.Database
import ch.rmy.android.http_shortcuts.data.domains.BaseRepository
import ch.rmy.android.http_shortcuts.data.enums.SyncSchedule
import ch.rmy.android.http_shortcuts.data.enums.SyncType
import ch.rmy.android.http_shortcuts.data.models.SyncConfig
import java.time.Instant
import javax.inject.Inject

class SyncRepository
@Inject
constructor(
    database: Database,
) : BaseRepository(database) {
    suspend fun getConfig(syncType: SyncType) = query {
        syncDao().getSyncConfig(syncType)
            ?: SyncConfig(
                id = syncType.value,
                type = syncType,
                schedule = SyncSchedule.WEEKLY,
            )
    }

    fun observeConfig(syncType: SyncType) = queryFlow {
        syncDao().observeSyncConfig(syncType)
    }

    suspend fun updateConfig(syncConfig: SyncConfig) = query {
        syncDao().insertOrReplace(syncConfig)
    }

    suspend fun setLastSucceeded(syncType: SyncType, time: Instant) = query {
        syncDao().setLastSucceeded(syncType, time)
    }

    suspend fun setLastFailed(syncType: SyncType, time: Instant) = query {
        syncDao().setLastFailed(syncType, time)
    }
}

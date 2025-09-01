package ch.rmy.android.http_shortcuts.data.domains.sync

import ch.rmy.android.http_shortcuts.data.Database
import ch.rmy.android.http_shortcuts.data.domains.BaseRepository
import ch.rmy.android.http_shortcuts.data.enums.SyncSchedule
import ch.rmy.android.http_shortcuts.data.enums.SyncType
import ch.rmy.android.http_shortcuts.data.models.SyncConfig
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
                target = "",
                schedule = SyncSchedule.WEEKLY,
            )
    }

    suspend fun updateConfig(syncConfig: SyncConfig) = query {
        syncDao().insertOrReplace(syncConfig)
    }
}

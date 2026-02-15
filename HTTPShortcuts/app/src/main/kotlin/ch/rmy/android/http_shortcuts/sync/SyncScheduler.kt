package ch.rmy.android.http_shortcuts.sync

import androidx.work.WorkInfo
import ch.rmy.android.http_shortcuts.data.domains.sync.SyncRepository
import ch.rmy.android.http_shortcuts.data.enums.SyncSchedule
import ch.rmy.android.http_shortcuts.data.enums.SyncTargetType
import ch.rmy.android.http_shortcuts.data.settings.UserPreferences
import javax.inject.Inject
import kotlin.time.Duration.Companion.days
import kotlinx.coroutines.flow.Flow

class SyncScheduler
@Inject
constructor(
    private val userPreferences: UserPreferences,
    private val syncRepository: SyncRepository,
    private val syncWorkerStarter: SyncWorker.Starter,
) {
    suspend fun schedule() {
        syncWorkerStarter.cancel()

        val syncType = userPreferences.syncType
            ?: return

        val syncConfig = syncRepository.getConfig(syncType)
        if (!syncConfig.isValid) {
            return
        }

        syncWorkerStarter.scheduleRepeating(
            interval = when (syncConfig.schedule) {
                SyncSchedule.DAILY -> 1.days
                SyncSchedule.WEEKLY -> 7.days
            },
            requiresNetwork = syncConfig.targetType == SyncTargetType.URL,
        )
    }

    suspend fun syncNow() {
        syncWorkerStarter.scheduleNow()
    }

    fun observeState(): Flow<WorkInfo.State?> =
        syncWorkerStarter.observeState()
}

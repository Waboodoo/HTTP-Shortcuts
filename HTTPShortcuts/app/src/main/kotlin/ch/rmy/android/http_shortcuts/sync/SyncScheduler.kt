package ch.rmy.android.http_shortcuts.sync

import androidx.work.WorkInfo
import ch.rmy.android.http_shortcuts.data.domains.sync.SyncRepository
import ch.rmy.android.http_shortcuts.data.enums.SyncSchedule
import ch.rmy.android.http_shortcuts.data.enums.SyncTargetType
import ch.rmy.android.http_shortcuts.data.settings.UserPreferences
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.seconds
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
                SyncSchedule.ON_CHANGE -> return
            },
            requiresNetwork = syncConfig.targetType == SyncTargetType.URL,
        )
    }

    suspend fun syncNow() {
        syncWorkerStarter.scheduleOnce(delay = Duration.ZERO)
    }

    suspend fun syncSoonOnChangesIfNeeded() {
        val syncType = userPreferences.syncType ?: return
        val syncConfig = syncRepository.getConfig(syncType)
        if (!syncConfig.isValid || syncConfig.schedule != SyncSchedule.ON_CHANGE) {
            return
        }
        syncWorkerStarter.scheduleOnce(
            delay = 10.seconds,
            requiresNetwork = syncConfig.targetType == SyncTargetType.URL,
        )
    }

    fun observeState(): Flow<WorkInfo.State?> =
        syncWorkerStarter.observeState()
}

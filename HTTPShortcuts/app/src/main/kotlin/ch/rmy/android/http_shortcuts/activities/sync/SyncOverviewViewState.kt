package ch.rmy.android.http_shortcuts.activities.sync

import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.activities.sync.models.SyncState
import ch.rmy.android.http_shortcuts.data.enums.SyncType
import java.time.LocalDateTime

@Stable
data class SyncOverviewViewState(
    val syncType: SyncType?,
    val isConfigValid: Boolean,
    val isSyncing: Boolean = false,
    val importLastSucceeded: LocalDateTime?,
    val importLastFailed: LocalDateTime?,
    val exportLastSucceeded: LocalDateTime?,
    val exportLastFailed: LocalDateTime?,
) {
    val syncState: SyncState?
        get() {
            if (syncType == null || !isConfigValid) {
                return null
            }
            return if (isSyncing) {
                SyncState.SYNCING
            } else {
                SyncState.IDLE
            }
        }
}

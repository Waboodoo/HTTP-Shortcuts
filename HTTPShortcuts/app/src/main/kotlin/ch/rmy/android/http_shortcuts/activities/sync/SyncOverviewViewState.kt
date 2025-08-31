package ch.rmy.android.http_shortcuts.activities.sync

import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.data.enums.SyncType

@Stable
data class SyncOverviewViewState(
    val syncType: SyncType,
)

package ch.rmy.android.http_shortcuts.activities.sync

import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.data.enums.SyncSchedule

@Stable
data class SyncExportViewState(
    val schedule: SyncSchedule,
    val password: String,
)

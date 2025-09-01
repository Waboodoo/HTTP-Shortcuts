package ch.rmy.android.http_shortcuts.activities.sync

import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.data.enums.SyncSchedule

@Stable
data class SyncImportViewState(
    val schedule: SyncSchedule,
    val password: String,
)

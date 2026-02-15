package ch.rmy.android.http_shortcuts.activities.sync

import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.data.enums.SyncSchedule
import ch.rmy.android.http_shortcuts.data.enums.SyncTargetType

@Stable
data class SyncExportViewState(
    val schedule: SyncSchedule,
    val targetType: SyncTargetType,
    val filePassword: String,
    val directoryName: String,
    val fileName: String,
    val webUrl: String,
    val webAuthUsername: String,
    val webAuthPassword: String,
    val hasChanged: Boolean,
)

package ch.rmy.android.http_shortcuts.activities.sync

import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.data.enums.SyncType

@Stable
data class SyncSettingsViewState(
    val syncType: SyncType,
)

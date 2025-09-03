package ch.rmy.android.http_shortcuts.activities.sync.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.SelectionField
import ch.rmy.android.http_shortcuts.data.enums.SyncSchedule

@Composable
fun SyncScheduleSelector(
    modifier: Modifier,
    label: String,
    syncSchedule: SyncSchedule,
    onSyncScheduleChanged: (SyncSchedule) -> Unit,
) {
    SelectionField(
        modifier = modifier,
        title = label,
        selectedKey = syncSchedule,
        items = listOf(
            SyncSchedule.DAILY to stringResource(R.string.sync_schedule_every_day),
            SyncSchedule.WEEKLY to stringResource(R.string.sync_schedule_every_week),
        ),
        onItemSelected = onSyncScheduleChanged,
    )
}

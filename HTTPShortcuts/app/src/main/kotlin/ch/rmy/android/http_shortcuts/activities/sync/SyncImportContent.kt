package ch.rmy.android.http_shortcuts.activities.sync

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.sync.components.PasswordProtection
import ch.rmy.android.http_shortcuts.activities.sync.components.SyncScheduleSelector
import ch.rmy.android.http_shortcuts.components.Spacing
import ch.rmy.android.http_shortcuts.components.VerticalSpacer
import ch.rmy.android.http_shortcuts.data.enums.SyncSchedule

@Composable
fun SyncImportContent(
    viewState: SyncImportViewState,
    onScheduleChanged: (SyncSchedule) -> Unit,
    onPasswordChanged: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.MEDIUM)
            .verticalScroll(rememberScrollState()),
    ) {
        SyncScheduleSelector(
            modifier = Modifier.fillMaxWidth(),
            label = stringResource(R.string.label_export_schedule),
            syncSchedule = viewState.schedule,
            onSyncScheduleChanged = onScheduleChanged,
        )

        VerticalSpacer(Spacing.SMALL)

        PasswordProtection(
            modifier = Modifier.fillMaxWidth(),
            password = viewState.password,
            onPasswordChanged = onPasswordChanged,
        )
    }
}

package ch.rmy.android.http_shortcuts.activities.sync

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.SimpleScaffold
import ch.rmy.android.http_shortcuts.components.bindViewModel
import ch.rmy.android.http_shortcuts.data.enums.SyncType

@Composable
fun SyncSettingsScreen() {
    val (viewModel, state) = bindViewModel<SyncSettingsViewState, SyncSettingsViewModel>()

    SimpleScaffold(
        viewState = state,
        title = when (state?.syncType) {
            SyncType.IMPORT -> stringResource(R.string.sync_type_automatic_import)
            SyncType.EXPORT -> stringResource(R.string.sync_type_automatic_export)
            null -> ""
        },
    ) { viewState ->
        SyncSettingsContent(
            viewState,
        )
    }
}

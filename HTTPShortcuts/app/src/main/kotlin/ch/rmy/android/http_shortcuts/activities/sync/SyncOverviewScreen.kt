package ch.rmy.android.http_shortcuts.activities.sync

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.SimpleScaffold
import ch.rmy.android.http_shortcuts.components.bindViewModel

@Composable
fun SyncOverviewScreen() {
    val (viewModel, state) = bindViewModel<SyncOverviewViewState, SyncOverviewViewModel>()

    SimpleScaffold(
        viewState = state,
        title = stringResource(R.string.settings_automatic_import_export),
    ) { viewState ->
        SyncOverviewContent(
            viewState,
            onSyncTypeSelected = viewModel::onSyncTypeSelected,
            onConfigureImportClicked = viewModel::onConfigureImportClicked,
            onConfigureExportClicked = viewModel::onConfigureExportClicked,
        )
    }
}

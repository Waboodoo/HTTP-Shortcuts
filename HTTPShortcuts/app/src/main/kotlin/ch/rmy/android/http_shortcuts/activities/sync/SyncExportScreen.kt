package ch.rmy.android.http_shortcuts.activities.sync

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.SimpleScaffold
import ch.rmy.android.http_shortcuts.components.bindViewModel

@Composable
fun SyncExportScreen() {
    val (viewModel, state) = bindViewModel<SyncExportViewState, SyncExportViewModel>()

    SimpleScaffold(
        viewState = state,
        title = stringResource(R.string.sync_type_automatic_export),
    ) { viewState ->
        SyncExportContent(
            viewState = viewState,
            onPasswordChanged = viewModel::onPasswordChanged,
        )
    }
}

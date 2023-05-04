package ch.rmy.android.http_shortcuts.activities.editor.headers

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.FloatingAddButton
import ch.rmy.android.http_shortcuts.components.ScreenScope
import ch.rmy.android.http_shortcuts.components.SimpleScaffold
import ch.rmy.android.http_shortcuts.components.bindViewModel

@Composable
fun ScreenScope.RequestHeadersScreen() {
    val (viewModel, state) = bindViewModel<RequestHeadersViewState, RequestHeadersViewModel>()

    BackHandler {
        viewModel.onBackPressed()
    }

    SimpleScaffold(
        viewState = state,
        title = stringResource(R.string.section_request_headers),
        floatingActionButton = {
            FloatingAddButton(onClick = viewModel::onAddHeaderButtonClicked)
        },
    ) { viewState ->
        RequestHeadersContent(
            headers = viewState.headerItems,
            onHeaderClicked = viewModel::onHeaderClicked,
            onHeaderMoved = viewModel::onHeaderMoved,
        )
    }

    RequestHeadersDialogs(
        dialogState = state?.dialogState,
        onConfirmed = viewModel::onDialogConfirmed,
        onDelete = viewModel::onRemoveHeaderButtonClicked,
        onDismissed = viewModel::onDismissDialog,
    )
}

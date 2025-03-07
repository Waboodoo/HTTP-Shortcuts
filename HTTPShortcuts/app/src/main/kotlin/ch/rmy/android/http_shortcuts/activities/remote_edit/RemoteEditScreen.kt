package ch.rmy.android.http_shortcuts.activities.remote_edit

import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.SimpleScaffold
import ch.rmy.android.http_shortcuts.components.ToolbarIcon
import ch.rmy.android.http_shortcuts.components.bindViewModel

@Composable
fun RemoteEditScreen() {
    val (viewModel, state) = bindViewModel<RemoteEditViewState, RemoteEditViewModel>()

    BackHandler(state != null) {
        viewModel.onBackPressed()
    }

    SimpleScaffold(
        viewState = state,
        title = stringResource(R.string.settings_remote_edit),
        actions = {
            ToolbarIcon(
                Icons.Filled.Settings,
                contentDescription = stringResource(R.string.button_change_remote_server),
                onClick = viewModel::onChangeRemoteHostButtonClicked,
            )
        },
    ) { viewState ->
        RemoteEditContent(
            viewState,
            onPasswordChanged = viewModel::onPasswordChanged,
            onUploadButtonClicked = viewModel::onUploadButtonClicked,
            onDownloadButtonClicked = viewModel::onDownloadButtonClicked,
        )
    }

    RemoteEditDialog(
        state?.dialogState,
        onDismissRequest = viewModel::onDialogDismissalRequested,
        onServerUrlChange = viewModel::onServerUrlChange,
    )
}

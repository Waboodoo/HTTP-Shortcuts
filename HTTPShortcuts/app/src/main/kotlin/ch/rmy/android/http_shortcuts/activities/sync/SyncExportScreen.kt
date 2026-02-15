package ch.rmy.android.http_shortcuts.activities.sync

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.SimpleScaffold
import ch.rmy.android.http_shortcuts.components.ToolbarIcon
import ch.rmy.android.http_shortcuts.components.bindViewModel
import ch.rmy.android.http_shortcuts.utils.PickDirectoryContract

@Composable
fun SyncExportScreen() {
    val (viewModel, state) = bindViewModel<SyncExportViewState, SyncExportViewModel>()

    BackHandler(enabled = state?.hasChanged == true) {
        viewModel.onBackPressed()
    }

    val context = LocalContext.current
    val pickDirectory = rememberLauncherForActivityResult(PickDirectoryContract) { getDirectoryUri ->
        getDirectoryUri(context.contentResolver)?.let(viewModel::onDirectoryPicked)
    }

    SimpleScaffold(
        viewState = state,
        title = stringResource(R.string.sync_type_automatic_export),
        actions = {
            ToolbarIcon(
                painterResource(R.drawable.outline_help_24),
                contentDescription = stringResource(R.string.button_show_help),
                onClick = viewModel::onHelpButtonClicked,
            )
        },
    ) { viewState ->
        SyncExportContent(
            viewState = viewState,
            onSyncCategoryChecked = viewModel::onSyncCategoryChecked,
            onScheduleChanged = viewModel::onScheduleChanged,
            onFilePasswordChanged = viewModel::onFilePasswordChanged,
            onTargetTypeChanged = viewModel::onTargetTypeChanged,
            onDirectoryClicked = { pickDirectory.launch(null) },
            onFileNameChanged = viewModel::onFileNameChanged,
            onWebUrlChanged = viewModel::onWebUrlChanged,
            onWebAuthUsernameChanged = viewModel::onWebAuthUsernameChanged,
            onWebAuthPasswordChanged = viewModel::onWebAuthPasswordChanged,
        )
    }
}

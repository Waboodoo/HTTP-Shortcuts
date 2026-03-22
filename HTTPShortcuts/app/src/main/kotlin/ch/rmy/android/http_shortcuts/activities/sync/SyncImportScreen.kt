package ch.rmy.android.http_shortcuts.activities.sync

import android.content.ActivityNotFoundException
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import ch.rmy.android.framework.extensions.showToast
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.SimpleScaffold
import ch.rmy.android.http_shortcuts.components.ToolbarIcon
import ch.rmy.android.http_shortcuts.components.bindViewModel
import ch.rmy.android.http_shortcuts.utils.PickDirectoryContract

@Composable
fun SyncImportScreen() {
    val (viewModel, state) = bindViewModel<SyncImportViewState, SyncImportViewModel>()

    BackHandler(enabled = state?.hasChanged == true) {
        viewModel.onBackPressed()
    }

    val context = LocalContext.current
    val pickDirectory = rememberLauncherForActivityResult(PickDirectoryContract) { getDirectoryUri ->
        getDirectoryUri(context.contentResolver)?.let(viewModel::onDirectoryPicked)
    }

    SimpleScaffold(
        viewState = state,
        title = stringResource(R.string.sync_type_automatic_import),
        actions = {
            ToolbarIcon(
                painterResource(R.drawable.outline_help_24),
                contentDescription = stringResource(R.string.button_show_help),
                onClick = viewModel::onHelpButtonClicked,
            )
        },
    ) { viewState ->
        SyncImportContent(
            viewState = viewState,
            onScheduleChanged = viewModel::onScheduleChanged,
            onFilePasswordChanged = viewModel::onFilePasswordChanged,
            onTargetTypeChanged = viewModel::onTargetTypeChanged,
            onDirectoryClicked = {
                try {
                    pickDirectory.launch(null)
                } catch (_: ActivityNotFoundException) {
                    context.showToast(R.string.error_not_supported)
                }
            },
            onFileNameChanged = viewModel::onFileNameChanged,
            onWebUrlChanged = viewModel::onWebUrlChanged,
            onWebAuthUsernameChanged = viewModel::onWebAuthUsernameChanged,
            onWebAuthPasswordChanged = viewModel::onWebAuthPasswordChanged,
            onReplaceLocalChanged = viewModel::onReplaceLocalChanged,
        )
    }
}

package ch.rmy.android.http_shortcuts.activities.workingdirectories

import android.content.ActivityNotFoundException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import ch.rmy.android.framework.extensions.consume
import ch.rmy.android.framework.extensions.showToast
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.EventHandler
import ch.rmy.android.http_shortcuts.components.FloatingAddButton
import ch.rmy.android.http_shortcuts.components.ScreenInstructionsHeaders
import ch.rmy.android.http_shortcuts.components.SimpleScaffold
import ch.rmy.android.http_shortcuts.components.ToolbarIcon
import ch.rmy.android.http_shortcuts.components.bindViewModel
import ch.rmy.android.http_shortcuts.utils.PickDirectoryContract

@Composable
fun WorkingDirectoriesScreen(
    picker: Boolean,
) {
    val (viewModel, state) = bindViewModel<WorkingDirectoriesViewModel.InitData, WorkingDirectoriesViewState, WorkingDirectoriesViewModel>(
        WorkingDirectoriesViewModel.InitData(picker = picker),
    )

    val context = LocalContext.current

    val pickDirectory = rememberLauncherForActivityResult(PickDirectoryContract) { getDirectoryUri ->
        getDirectoryUri(context.contentResolver)?.let(viewModel::onDirectoryPicked)
    }

    EventHandler { event ->
        when (event) {
            is WorkingDirectoriesEvent.OpenDirectoryPicker -> consume {
                try {
                    pickDirectory.launch(event.initialDirectory)
                } catch (e: ActivityNotFoundException) {
                    context.showToast(R.string.error_not_supported)
                }
            }
            else -> false
        }
    }

    SimpleScaffold(
        viewState = state,
        title = stringResource(R.string.title_working_directories),
        actions = {
            ToolbarIcon(
                Icons.AutoMirrored.Filled.HelpOutline,
                contentDescription = stringResource(R.string.button_show_help),
                onClick = viewModel::onHelpButtonClicked,
            )
        },
        floatingActionButton = {
            FloatingAddButton(
                onClick = viewModel::onCreateButtonClicked,
                contentDescription = stringResource(R.string.accessibility_label_mount_working_directory_fab),
            )
        },
    ) { viewState ->
        Column {
            if (picker) {
                ScreenInstructionsHeaders(stringResource(R.string.instructions_select_directory))
            }
            WorkingDirectoriesContent(
                workingDirectories = viewState.workingDirectories,
                onWorkingDirectoryClicked = viewModel::onWorkingDirectoryClicked,
            )
        }
    }

    WorkingDirectoriesDialogs(
        state?.dialogState,
        onRenameClicked = viewModel::onRenameClicked,
        onRenameConfirmed = viewModel::onRenameConfirmed,
        onMountClicked = viewModel::onMountClicked,
        onDeleteClicked = viewModel::onDeleteClicked,
        onDeleteConfirmed = viewModel::onDeleteConfirmed,
        onDismissRequest = viewModel::onDialogDismissalRequested,
    )
}

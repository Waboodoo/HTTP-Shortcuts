package ch.rmy.android.http_shortcuts.activities.importexport

import android.content.ActivityNotFoundException
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.SavedStateHandle
import ch.rmy.android.framework.extensions.consume
import ch.rmy.android.framework.extensions.showToast
import ch.rmy.android.framework.utils.FilePickerUtil
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.EventHandler
import ch.rmy.android.http_shortcuts.components.SimpleScaffold
import ch.rmy.android.http_shortcuts.components.ToolbarIcon
import ch.rmy.android.http_shortcuts.components.bindViewModel
import ch.rmy.android.http_shortcuts.navigation.NavigationDestination.RemoteEdit.RESULT_CHANGES_IMPORTED
import ch.rmy.android.http_shortcuts.navigation.ResultHandler

@Composable
fun ImportExportScreen(
    savedStateHandle: SavedStateHandle,
    importUrl: Uri?,
) {
    val context = LocalContext.current

    val (viewModel, state) = bindViewModel<ImportExportViewModel.InitData, ImportExportViewState, ImportExportViewModel>(
        ImportExportViewModel.InitData(importUrl),
    )

    BackHandler(state != null) {
        viewModel.onBackPressed()
    }

    ResultHandler(savedStateHandle) { result ->
        if (result == RESULT_CHANGES_IMPORTED) {
            viewModel.onRemoteEditorChangesImported()
        }
    }

    val openFilePickerForImport = rememberLauncherForActivityResult(FilePickerUtil.PickFile) { fileUri ->
        fileUri?.let(viewModel::onFilePickedForImport)
    }

    EventHandler { event ->
        when (event) {
            is ImportExportEvent.OpenFilePickerForImport -> consume {
                try {
                    openFilePickerForImport.launch(null)
                } catch (e: ActivityNotFoundException) {
                    context.showToast(R.string.error_not_supported)
                }
            }
            else -> false
        }
    }

    SimpleScaffold(
        viewState = state,
        title = stringResource(R.string.title_import_export),
        actions = {
            ToolbarIcon(
                Icons.AutoMirrored.Filled.HelpOutline,
                contentDescription = stringResource(R.string.button_show_help),
                onClick = viewModel::onHelpButtonClicked,
            )
        },
    ) { viewState ->
        ImportExportContent(
            exportEnabled = viewState.exportEnabled,
            onImportFromFileClicked = viewModel::onImportFromFileButtonClicked,
            onImportFromUrlClicked = viewModel::onImportFromURLButtonClicked,
            onExportToFileClicked = viewModel::onExportToFileButtonClicked,
            onExportViaShareClicked = viewModel::onExportViaShareButtonClicked,
            onRemoteEditButtonClicked = viewModel::onRemoteEditButtonClicked,
        )
    }

    ImportExportDialog(
        state?.dialogState,
        onImportFromUrl = viewModel::onImportFromUrlDialogSubmitted,
        onDismissRequest = viewModel::onDialogDismissalRequested,
    )
}

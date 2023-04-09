package ch.rmy.android.http_shortcuts.activities.importexport

import android.content.ActivityNotFoundException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import ch.rmy.android.framework.extensions.consume
import ch.rmy.android.framework.extensions.showToast
import ch.rmy.android.framework.utils.FilePickerUtil
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.ScreenScope
import ch.rmy.android.http_shortcuts.components.SimpleScaffold
import ch.rmy.android.http_shortcuts.components.ToolbarIcon
import ch.rmy.android.http_shortcuts.components.bindViewModel
import ch.rmy.android.http_shortcuts.import_export.OpenFilePickerForExportContract

@Composable
fun ScreenScope.ImportExportScreen(initData: ImportExportViewModel.InitData) {
    val context = LocalContext.current

    val (viewModel, state) = bindViewModel<ImportExportViewModel.InitData, ImportExportViewState, ImportExportViewModel>(initData)

    val openFilePickerForExport = rememberLauncherForActivityResult(OpenFilePickerForExportContract) { fileUri ->
        fileUri?.let(viewModel::onFilePickedForExport)
    }
    val openFilePickerForImport = rememberLauncherForActivityResult(FilePickerUtil.PickFile) { fileUri ->
        fileUri?.let(viewModel::onFilePickedForImport)
    }

    EventHandler { event ->
        when (event) {
            is ImportExportEvent.OpenFilePickerForExport -> consume {
                try {
                    openFilePickerForExport.launch(
                        OpenFilePickerForExportContract.Params(
                            format = event.exportFormat,
                            single = false,
                        )
                    )
                } catch (e: ActivityNotFoundException) {
                    context.showToast(R.string.error_not_supported)
                }
            }
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
                Icons.Outlined.HelpOutline,
                contentDescription = stringResource(R.string.button_show_help),
                onClick = viewModel::onHelpButtonClicked,
            )
        },
    ) { viewState ->
        ImportExportContent(
            onRemoteEditorClosed = viewModel::onRemoteEditorClosed,
            useLegacyFormat = viewState.useLegacyFormat,
            onLegacyFormatUseChanged = viewModel::onLegacyFormatUseChanged,
            onImportFromFileClicked = viewModel::onImportFromFileButtonClicked,
            onImportFromUrlClicked = viewModel::onImportFromURLButtonClicked,
            onExportClicked = viewModel::onExportButtonClicked,
        )
    }

    ImportExportDialog(
        state?.dialogState,
        onImportFromUrl = viewModel::onImportFromUrlDialogSubmitted,
        onShortcutsSelectedForExport = viewModel::onShortcutsForExportSelected,
        onDismissRequest = viewModel::onDialogDismissalRequested,
        onExportToFileOptionSelected = viewModel::onExportToFileOptionSelected,
        onExportViaSharingOptionSelected = viewModel::onExportViaSharingOptionSelected,
    )
}

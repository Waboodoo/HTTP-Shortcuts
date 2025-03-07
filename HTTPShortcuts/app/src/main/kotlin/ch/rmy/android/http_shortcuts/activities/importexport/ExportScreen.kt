package ch.rmy.android.http_shortcuts.activities.importexport

import android.content.ActivityNotFoundException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Deselect
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import ch.rmy.android.framework.extensions.consume
import ch.rmy.android.framework.extensions.showToast
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.EventHandler
import ch.rmy.android.http_shortcuts.components.SimpleScaffold
import ch.rmy.android.http_shortcuts.components.ToolbarIcon
import ch.rmy.android.http_shortcuts.components.bindViewModel
import ch.rmy.android.http_shortcuts.import_export.OpenFilePickerForExportContract

@Composable
fun ExportScreen(toFile: Boolean) {
    val context = LocalContext.current
    val (viewModel, state) = bindViewModel<ExportViewModel.InitData, ExportViewState, ExportViewModel>(ExportViewModel.InitData(toFile))

    val openFilePickerForExport = rememberLauncherForActivityResult(OpenFilePickerForExportContract) { fileUri ->
        fileUri?.let(viewModel::onFilePickedForExport)
    }

    EventHandler { event ->
        when (event) {
            is ExportEvent.OpenFilePickerForExport -> consume {
                try {
                    openFilePickerForExport.launch(
                        OpenFilePickerForExportContract.Params(),
                    )
                } catch (e: ActivityNotFoundException) {
                    context.showToast(R.string.error_not_supported)
                }
            }
            else -> false
        }
    }

    SimpleScaffold(
        viewState = state,
        title = stringResource(R.string.settings_title_export),
        actions = { viewState ->
            if (viewState.isSelectAllEnabled) {
                ToolbarIcon(
                    Icons.Filled.SelectAll,
                    contentDescription = stringResource(R.string.accessibility_label_select_all_for_export),
                    onClick = viewModel::onSelectAllButtonClicked,
                )
            } else {
                ToolbarIcon(
                    Icons.Filled.Deselect,
                    contentDescription = stringResource(R.string.accessibility_label_unselect_all_for_export),
                    onClick = viewModel::onDeselectAllButtonClicked,
                )
            }
            ToolbarIcon(
                Icons.Filled.Check,
                enabled = viewState.isExportEnabled,
                contentDescription = stringResource(R.string.dialog_button_export),
                onClick = viewModel::onExportButtonClicked,
            )
        },
    ) { viewState ->
        ExportContent(
            items = viewState.items,
            onShortcutCheckedChanged = viewModel::onShortcutCheckedChanged,
            onCategoryCheckedChanged = viewModel::onCategoryCheckedChanged,
        )
    }

    ExportDialog(
        state?.dialogState,
        onDismissRequest = viewModel::onDialogDismissalRequested,
    )
}

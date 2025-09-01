package ch.rmy.android.http_shortcuts.activities.importexport

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.launch
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.statusBarsIgnoringVisibility
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import ch.rmy.android.framework.extensions.consume
import ch.rmy.android.framework.extensions.showToast
import ch.rmy.android.framework.extensions.startActivity
import ch.rmy.android.framework.utils.FilePickerUtil
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.EventHandler
import ch.rmy.android.http_shortcuts.components.bindViewModel
import ch.rmy.android.http_shortcuts.import_export.OpenFilePickerForExportContract
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ImportExportScreen() {
    val context = LocalContext.current
    val activity = LocalActivity.current

    val (viewModel, state) = bindViewModel<ImportExportViewState, ImportExportViewModel>()

    BackHandler(state != null) {
        viewModel.onBackPressed()
    }

    val openFilePickerForImport = rememberLauncherForActivityResult(FilePickerUtil.PickFile) { fileUri ->
        fileUri?.let(viewModel::onFilePickedForImport)
    }

    val openFilePickerForExport = rememberLauncherForActivityResult(OpenFilePickerForExportContract) { fileUri ->
        fileUri?.let(viewModel::onFilePickedForExport)
    }
    val coroutineScope = rememberCoroutineScope()

    EventHandler { event ->
        when (event) {
            is ImportExportEvent.OpenFilePickerForImport -> consume {
                try {
                    openFilePickerForImport.launch(null)
                } catch (_: ActivityNotFoundException) {
                    context.showToast(R.string.error_not_supported)
                }
            }

            is ImportExportEvent.SendExport -> consume {
                val activity = activity ?: return@consume
                coroutineScope.launch {
                    Intent(Intent.ACTION_SEND)
                        .setClassName(
                            "ch.rmy.android.http_shortcuts",
                            "ch.rmy.android.http_shortcuts.activities.main.MainActivity",
                        )
                        .addCategory(Intent.CATEGORY_DEFAULT)
                        .setType("application/zip")
                        .putExtra(Intent.EXTRA_STREAM, event.export)
                        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        .startActivity(activity)
                    activity.finish()
                }
            }

            else -> false
        }
    }

    LaunchedEffect(Unit) {
        activity?.intent?.extras?.getParcelable<Uri>(Intent.EXTRA_STREAM)
            ?.let { importUri ->
                viewModel.onExternalFileReceived(importUri)
            }
    }

    Box(
        modifier = Modifier
            .imePadding()
            .windowInsetsPadding(WindowInsets.statusBarsIgnoringVisibility),
    ) {
        ImportExportContent(
            importStatus = state?.importStatus,
            onImportFromFileClicked = viewModel::onImportFromFileButtonClicked,
            onExportToFileClicked = {
                try {
                    openFilePickerForExport.launch()
                } catch (_: ActivityNotFoundException) {
                    context.showToast(R.string.error_not_supported)
                }
            },
        )
    }

    ImportExportDialog(
        state?.dialogState,
        onImportPasswordSubmitted = viewModel::onImportPasswordSubmitted,
        onDismissRequest = viewModel::onDialogDismissalRequested,
    )
}

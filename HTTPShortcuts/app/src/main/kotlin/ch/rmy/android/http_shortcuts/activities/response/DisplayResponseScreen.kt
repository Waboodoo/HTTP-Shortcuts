package ch.rmy.android.http_shortcuts.activities.response

import android.content.ActivityNotFoundException
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ch.rmy.android.framework.extensions.consume
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.BackButton
import ch.rmy.android.http_shortcuts.components.EventHandler
import ch.rmy.android.http_shortcuts.components.ProgressDialog
import ch.rmy.android.http_shortcuts.components.SimpleScaffold
import ch.rmy.android.http_shortcuts.components.ToolbarIcon
import ch.rmy.android.http_shortcuts.components.bindViewModel
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.enums.ResponseDisplayAction
import java.nio.charset.Charset
import kotlin.time.Duration

@Composable
fun DisplayResponseScreen(
    shortcutId: ShortcutId,
    shortcutName: String,
    text: String?,
    mimeType: String?,
    charset: Charset,
    url: Uri?,
    fileUri: Uri?,
    statusCode: Int?,
    headers: Map<String, List<String>>,
    timing: Duration?,
    showDetails: Boolean,
    monospace: Boolean,
    actions: List<ResponseDisplayAction>,
) {
    val (viewModel, state) = bindViewModel<DisplayResponseViewModel.InitData, DisplayResponseViewState, DisplayResponseViewModel>(
        DisplayResponseViewModel.InitData(
            shortcutId, shortcutName, text, mimeType, charset, url, fileUri, statusCode, headers, timing, showDetails, monospace, actions,
        )
    )

    val openFilePicker = rememberLauncherForActivityResult(SaveFileContract) { file ->
        file?.let(viewModel::onFilePickedForSaving)
    }

    EventHandler { event ->
        when (event) {
            is DisplayResponseEvent.PickFileForSaving -> consume {
                try {
                    openFilePicker.launch(SaveFileContract.Params(type = event.mimeType, title = shortcutName))
                } catch (e: ActivityNotFoundException) {
                    viewModel.onFilePickerFailed()
                }
            }
            else -> false
        }
    }

    SimpleScaffold(
        viewState = state,
        title = shortcutName,
        backButton = BackButton.CROSS,
        actions = { viewState ->
            actions.forEach { action ->
                when (action) {
                    ResponseDisplayAction.RERUN -> {
                        ToolbarIcon(
                            Icons.Filled.Refresh,
                            contentDescription = stringResource(R.string.action_rerun_shortcut),
                            onClick = viewModel::onRerunButtonClicked,
                        )
                    }
                    ResponseDisplayAction.SHARE -> {
                        if (viewState.canShare) {
                            ToolbarIcon(
                                Icons.Filled.Share,
                                contentDescription = stringResource(R.string.share_button),
                                onClick = viewModel::onShareButtonClicked,
                            )
                        }
                    }
                    ResponseDisplayAction.COPY -> {
                        if (viewState.canCopy) {
                            ToolbarIcon(
                                Icons.Filled.FileCopy,
                                contentDescription = stringResource(R.string.action_copy_response),
                                onClick = viewModel::onCopyButtonClicked,
                            )
                        }
                    }
                    ResponseDisplayAction.SAVE -> {
                        if (viewState.canSave) {
                            ToolbarIcon(
                                Icons.Filled.Save,
                                contentDescription = stringResource(R.string.button_save_response_as_file),
                                onClick = viewModel::onSaveButtonClicked,
                            )
                        }
                    }
                }
            }
        },
    ) { viewState ->
        DisplayResponseContent(
            detailInfo = viewState.detailInfo,
            text = viewState.text,
            mimeType = viewState.mimeType,
            fileUri = viewState.fileUri,
            url = viewState.url,
            limitExceeded = viewState.limitExceeded,
            monospace = viewState.monospace,
        )
    }

    if (state?.isSaving == true) {
        ProgressDialog(
            text = stringResource(R.string.saving_in_progress),
            onDismissRequest = viewModel::onDialogDismissed,
        )
    }
}

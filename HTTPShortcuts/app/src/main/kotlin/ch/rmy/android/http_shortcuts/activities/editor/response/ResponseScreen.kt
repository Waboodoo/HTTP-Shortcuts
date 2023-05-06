package ch.rmy.android.http_shortcuts.activities.editor.response

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import ch.rmy.android.framework.extensions.consume
import ch.rmy.android.framework.extensions.launch
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.ScreenScope
import ch.rmy.android.http_shortcuts.components.SimpleScaffold
import ch.rmy.android.http_shortcuts.components.bindViewModel
import ch.rmy.android.http_shortcuts.extensions.localize
import ch.rmy.android.http_shortcuts.utils.PickDirectoryContract

@Composable
fun ScreenScope.ResponseScreen() {
    val (viewModel, state) = bindViewModel<ResponseViewState, ResponseViewModel>()

    val context = LocalContext.current
    val pickDirectory = rememberLauncherForActivityResult(PickDirectoryContract) { getDirectoryUri ->
        viewModel.onStoreFileDirectoryPicked(getDirectoryUri(context.contentResolver))
    }

    EventHandler { event ->
        when (event) {
            is ResponseEvent.PickDirectory -> consume {
                pickDirectory.launch()
            }
            else -> false
        }
    }

    BackHandler {
        viewModel.onBackPressed()
    }

    SimpleScaffold(
        viewState = state,
        title = stringResource(R.string.label_response_handling),
    ) { viewState ->
        ResponseContent(
            successMessageHint = viewState.successMessageHint.localize(),
            responseUiType = viewState.responseUiType,
            responseSuccessOutput = viewState.responseSuccessOutput,
            responseFailureOutput = viewState.responseFailureOutput,
            includeMetaInformation = viewState.includeMetaInformation,
            successMessage = viewState.successMessage,
            responseDisplayActions = viewState.responseDisplayActions,
            storeResponseIntoFile = viewState.storeResponseIntoFile,
            storeFileName = viewState.storeFileName,
            replaceFileIfExists = viewState.replaceFileIfExists,
            onResponseSuccessOutputChanged = viewModel::onResponseSuccessOutputChanged,
            onSuccessMessageChanged = viewModel::onSuccessMessageChanged,
            onResponseFailureOutputChanged = viewModel::onResponseFailureOutputChanged,
            onResponseUiTypeChanged = viewModel::onResponseUiTypeChanged,
            onDialogActionChanged = viewModel::onDialogActionChanged,
            onIncludeMetaInformationChanged = viewModel::onIncludeMetaInformationChanged,
            onShowActionButtonChanged = viewModel::onShowActionButtonChanged,
            onStoreResponseIntoFileChanged = viewModel::onStoreIntoFileCheckboxChanged,
            onReplaceFileIfExistsChanged = viewModel::onStoreFileOverwriteChanged,
            onStoreFileNameChanged = viewModel::onStoreFileNameChanged,
        )
    }
}

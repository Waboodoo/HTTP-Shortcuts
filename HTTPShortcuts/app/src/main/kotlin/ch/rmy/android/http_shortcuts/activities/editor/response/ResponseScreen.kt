package ch.rmy.android.http_shortcuts.activities.editor.response

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.SavedStateHandle
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.SimpleScaffold
import ch.rmy.android.http_shortcuts.components.bindViewModel
import ch.rmy.android.http_shortcuts.data.enums.ResponseUiType
import ch.rmy.android.http_shortcuts.extensions.localize
import ch.rmy.android.http_shortcuts.navigation.NavigationDestination
import ch.rmy.android.http_shortcuts.navigation.ResultHandler

@Composable
fun ResponseScreen(savedStateHandle: SavedStateHandle) {
    val (viewModel, state) = bindViewModel<ResponseViewState, ResponseViewModel>()

    val requestPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            viewModel.onResponseUiTypeChanged(ResponseUiType.NOTIFICATION)
        },
    )

    BackHandler(state != null) {
        viewModel.onBackPressed()
    }

    ResultHandler(savedStateHandle) { result ->
        when (result) {
            is NavigationDestination.WorkingDirectories.WorkingDirectoryPickerResult -> {
                viewModel.onWorkingDirectoryPicked(result.workingDirectoryId, result.name)
            }
        }
    }

    SimpleScaffold(
        viewState = state,
        title = stringResource(R.string.label_response_handling),
    ) { viewState ->
        ResponseContent(
            savedStateHandle = savedStateHandle,
            successMessageHint = viewState.successMessageHint.localize(),
            responseUiType = viewState.responseUiType,
            responseSuccessOutput = viewState.responseSuccessOutput,
            responseFailureOutput = viewState.responseFailureOutput,
            successMessage = viewState.successMessage,
            responseCharset = viewState.responseCharset,
            availableCharsets = viewState.availableCharsets,
            storeResponseIntoFile = viewState.storeResponseIntoFile,
            storeDirectoryName = viewState.storeDirectoryName,
            storeFileName = viewState.storeFileName,
            replaceFileIfExists = viewState.replaceFileIfExists,
            onResponseSuccessOutputChanged = viewModel::onResponseSuccessOutputChanged,
            onSuccessMessageChanged = viewModel::onSuccessMessageChanged,
            onResponseFailureOutputChanged = viewModel::onResponseFailureOutputChanged,
            onResponseUiTypeChanged = { responseUiType ->
                if (responseUiType == ResponseUiType.NOTIFICATION && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requestPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    viewModel.onResponseUiTypeChanged(responseUiType)
                }
            },
            onDisplaySettingsClicked = viewModel::onDisplaySettingsClicked,
            onResponseCharsetChanged = viewModel::onResponseCharsetChanged,
            onStoreResponseIntoFileChanged = viewModel::onStoreIntoFileCheckboxChanged,
            onReplaceFileIfExistsChanged = viewModel::onStoreFileOverwriteChanged,
            onStoreFileNameChanged = viewModel::onStoreFileNameChanged,
        )
    }
}

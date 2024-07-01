package ch.rmy.android.http_shortcuts.activities.editor.response

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.SavedStateHandle
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.SimpleScaffold
import ch.rmy.android.http_shortcuts.components.bindViewModel
import ch.rmy.android.http_shortcuts.extensions.localize
import ch.rmy.android.http_shortcuts.navigation.NavigationDestination
import ch.rmy.android.http_shortcuts.navigation.ResultHandler

@Composable
fun ResponseScreen(savedStateHandle: SavedStateHandle) {
    val (viewModel, state) = bindViewModel<ResponseViewState, ResponseViewModel>()

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
            successMessageHint = viewState.successMessageHint.localize(),
            responseUiType = viewState.responseUiType,
            responseSuccessOutput = viewState.responseSuccessOutput,
            responseFailureOutput = viewState.responseFailureOutput,
            successMessage = viewState.successMessage,
            storeResponseIntoFile = viewState.storeResponseIntoFile,
            storeDirectoryName = viewState.storeDirectoryName,
            storeFileName = viewState.storeFileName,
            replaceFileIfExists = viewState.replaceFileIfExists,
            onResponseSuccessOutputChanged = viewModel::onResponseSuccessOutputChanged,
            onSuccessMessageChanged = viewModel::onSuccessMessageChanged,
            onResponseFailureOutputChanged = viewModel::onResponseFailureOutputChanged,
            onResponseUiTypeChanged = viewModel::onResponseUiTypeChanged,
            onDisplaySettingsClicked = viewModel::onDisplaySettingsClicked,
            onStoreResponseIntoFileChanged = viewModel::onStoreIntoFileCheckboxChanged,
            onReplaceFileIfExistsChanged = viewModel::onStoreFileOverwriteChanged,
            onStoreFileNameChanged = viewModel::onStoreFileNameChanged,
        )
    }
}

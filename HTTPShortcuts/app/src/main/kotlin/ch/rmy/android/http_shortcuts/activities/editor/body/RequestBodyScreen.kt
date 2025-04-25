package ch.rmy.android.http_shortcuts.activities.editor.body

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.SavedStateHandle
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.FloatingAddButton
import ch.rmy.android.http_shortcuts.components.SimpleScaffold
import ch.rmy.android.http_shortcuts.components.bindViewModel
import ch.rmy.android.http_shortcuts.navigation.NavigationDestination
import ch.rmy.android.http_shortcuts.navigation.ResultHandler

@Composable
fun RequestBodyScreen(
    savedStateHandle: SavedStateHandle,
) {
    val (viewModel, state) = bindViewModel<RequestBodyViewState, RequestBodyViewModel>()

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
        title = stringResource(R.string.section_request_body),
        floatingActionButton = {
            AnimatedVisibility(
                visible = state?.addParameterButtonVisible == true,
                enter = scaleIn(),
                exit = scaleOut(),
            ) {
                FloatingAddButton(
                    onClick = viewModel::onAddParameterButtonClicked,
                    contentDescription = stringResource(R.string.accessibility_label_add_parameter_fab),
                )
            }
        },
    ) { viewState ->
        RequestBodyContent(
            savedStateHandle = savedStateHandle,
            requestBodyType = viewState.requestBodyType,
            fileUploadType = viewState.fileUploadType,
            sourceDirectoryName = viewState.sourceDirectoryName,
            sourceFileName = viewState.sourceFileName,
            parameters = viewState.parameters,
            contentType = viewState.contentType,
            bodyContent = viewState.bodyContent,
            bodyContentError = viewState.bodyContentError,
            syntaxHighlightingLanguage = viewState.syntaxHighlightingLanguage,
            useImageEditor = viewState.useImageEditor,
            fileNameSuggestions = viewState.fileNameSuggestions,
            onRequestBodyTypeChanged = viewModel::onRequestBodyTypeChanged,
            onFileUploadTypeChanged = viewModel::onFileUploadTypeChanged,
            onSourceDirectoryNameClicked = viewModel::onBodySourceDirectoryNameClicked,
            onSourceFileNameChanged = viewModel::onBodySourceFileNameChanged,
            onContentTypeChanged = viewModel::onContentTypeChanged,
            onBodyContentChanged = viewModel::onBodyContentChanged,
            onFormatButtonClicked = viewModel::onFormatButtonClicked,
            onParameterClicked = viewModel::onParameterClicked,
            onParameterMoved = viewModel::onParameterMoved,
            onUseImageEditorChanged = viewModel::onUseImageEditorChanged,
        )
    }

    RequestBodyDialogs(
        dialogState = state?.dialogState,
        savedStateHandle = savedStateHandle,
        onParameterTypeSelected = viewModel::onParameterTypeSelected,
        onParameterEdited = viewModel::onEditParameterDialogConfirmed,
        onParameterDeleted = viewModel::onRemoveParameterButtonClicked,
        onFileUploadTypeChanged = viewModel::onParameterFileUploadTypeChanged,
        onSourceDirectoryNameClicked = viewModel::onParameterSourceDirectoryNameClicked,
        onDismissed = viewModel::onDialogDismissed,
    )
}

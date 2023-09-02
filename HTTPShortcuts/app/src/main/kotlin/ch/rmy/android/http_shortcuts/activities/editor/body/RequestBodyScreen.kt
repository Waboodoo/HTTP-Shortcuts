package ch.rmy.android.http_shortcuts.activities.editor.body

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import ch.rmy.android.framework.extensions.consume
import ch.rmy.android.framework.extensions.launch
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.EventHandler
import ch.rmy.android.http_shortcuts.components.FloatingAddButton
import ch.rmy.android.http_shortcuts.components.SimpleScaffold
import ch.rmy.android.http_shortcuts.components.bindViewModel
import ch.rmy.android.http_shortcuts.utils.PickFileContract

@Composable
fun RequestBodyScreen() {
    val (viewModel, state) = bindViewModel<RequestBodyViewState, RequestBodyViewModel>()

    val context = LocalContext.current
    val pickFileForBody = rememberLauncherForActivityResult(PickFileContract) { getFileUri ->
        viewModel.onFilePickedForBody(getFileUri(context.contentResolver) ?: return@rememberLauncherForActivityResult)
    }
    val pickFileForParameter = rememberLauncherForActivityResult(PickFileContract) { getFileUri ->
        viewModel.onFilePickedForParameter(getFileUri(context.contentResolver) ?: return@rememberLauncherForActivityResult)
    }

    EventHandler { event ->
        when (event) {
            is RequestBodyEvent.PickFileForBody -> consume {
                pickFileForBody.launch()
            }
            is RequestBodyEvent.PickFileForParameter -> consume {
                pickFileForParameter.launch()
            }
            else -> false
        }
    }

    BackHandler(state != null) {
        viewModel.onBackPressed()
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
                FloatingAddButton(onClick = viewModel::onAddParameterButtonClicked)
            }
        },
    ) { viewState ->
        RequestBodyContent(
            requestBodyType = viewState.requestBodyType,
            fileUploadType = viewState.fileUploadType,
            fileName = viewState.fileName,
            parameters = viewState.parameters,
            contentType = viewState.contentType,
            bodyContent = viewState.bodyContent,
            bodyContentError = viewState.bodyContentError,
            syntaxHighlightingLanguage = viewState.syntaxHighlightingLanguage,
            useImageEditor = viewState.useImageEditor,
            onRequestBodyTypeChanged = viewModel::onRequestBodyTypeChanged,
            onFileUploadTypeChanged = viewModel::onFileUploadTypeChanged,
            onFileNameClicked = viewModel::onBodyFileNameClicked,
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
        onParameterTypeSelected = viewModel::onParameterTypeSelected,
        onParameterEdited = viewModel::onEditParameterDialogConfirmed,
        onParameterDeleted = viewModel::onRemoveParameterButtonClicked,
        onFileUploadTypeChanged = viewModel::onParameterFileUploadTypeChanged,
        onSourceFileNameClicked = viewModel::onParameterFileNameClicked,
        onDismissed = viewModel::onDialogDismissed,
    )
}

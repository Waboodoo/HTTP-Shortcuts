package ch.rmy.android.http_shortcuts.activities.editor.body

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.FloatingAddButton
import ch.rmy.android.http_shortcuts.components.ScreenScope
import ch.rmy.android.http_shortcuts.components.SimpleScaffold
import ch.rmy.android.http_shortcuts.components.bindViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ScreenScope.RequestBodyScreen() {
    val (viewModel, state) = bindViewModel<RequestBodyViewState, RequestBodyViewModel>()

    BackHandler {
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
            parameters = viewState.parameters,
            contentType = viewState.contentType,
            bodyContent = viewState.bodyContent,
            bodyContentError = viewState.bodyContentError,
            syntaxHighlightingLanguage = viewState.syntaxHighlightingLanguage,
            onRequestBodyTypeChanged = viewModel::onRequestBodyTypeChanged,
            onContentTypeChanged = viewModel::onContentTypeChanged,
            onBodyContentChanged = viewModel::onBodyContentChanged,
            onFormatButtonClicked = viewModel::onFormatButtonClicked,
            onParameterClicked = viewModel::onParameterClicked,
            onParameterMoved = viewModel::onParameterMoved,
        )
    }

    RequestBodyDialogs(
        dialogState = state?.dialogState,
        onParameterTypeSelected = viewModel::onParameterTypeSelected,
        onParameterEdited = viewModel::onEditParameterDialogConfirmed,
        onParameterDeleted = viewModel::onRemoveParameterButtonClicked,
        onDismissed = viewModel::onDialogDismissed,
    )
}

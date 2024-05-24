package ch.rmy.android.http_shortcuts.activities.editor.response

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.SimpleScaffold
import ch.rmy.android.http_shortcuts.components.bindViewModel
import ch.rmy.android.http_shortcuts.data.models.ResponseHandling

@Composable
fun ResponseDisplayScreen() {
    val (viewModel, state) = bindViewModel<ResponseDisplayViewState, ResponseDisplayViewModel>()

    BackHandler(state != null) {
        viewModel.onBackPressed()
    }

    SimpleScaffold(
        viewState = state,
        title = when (state?.responseSuccessOutput) {
            ResponseHandling.SUCCESS_OUTPUT_RESPONSE -> stringResource(R.string.title_response_display)
            ResponseHandling.SUCCESS_OUTPUT_MESSAGE -> stringResource(R.string.title_message_display)
            else -> ""
        },
    ) { viewState ->
        ResponseDisplayContent(
            responseUiType = viewState.responseUiType,
            responseSuccessOutput = viewState.responseSuccessOutput,
            responseContentType = viewState.responseContentType,
            responseCharset = viewState.responseCharset,
            availableCharsets = viewState.availableCharsets,
            includeMetaInformation = viewState.includeMetaInformation,
            responseDisplayActions = viewState.responseDisplayActions,
            useMonospaceFont = viewState.useMonospaceFont,
            fontSize = viewState.fontSize,
            jsonArrayAsTable = viewState.jsonArrayAsTable,
            onResponseContentTypeChanged = viewModel::onResponseContentTypeChanged,
            onResponseCharsetChanged = viewModel::onResponseCharsetChanged,
            onDialogActionChanged = viewModel::onDialogActionChanged,
            onIncludeMetaInformationChanged = viewModel::onIncludeMetaInformationChanged,
            onWindowActionsButtonClicked = viewModel::onWindowActionsButtonClicked,
            onUseMonospaceFontChanged = viewModel::onUseMonospaceFontChanged,
            onFontSizeChanged = viewModel::onFontSizeChanged,
            onJsonArrayAsTableChanged = viewModel::onJsonArrayAsTableChanged,
        )
    }

    ResponseDisplayDialogs(
        dialogState = state?.dialogState,
        onActionsSelected = viewModel::onWindowActionsSelected,
        onDismissed = viewModel::onDismissDialog,
    )
}

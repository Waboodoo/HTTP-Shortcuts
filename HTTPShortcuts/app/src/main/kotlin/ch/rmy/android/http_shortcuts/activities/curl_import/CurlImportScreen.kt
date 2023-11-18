package ch.rmy.android.http_shortcuts.activities.curl_import

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.BackButton
import ch.rmy.android.http_shortcuts.components.SimpleScaffold
import ch.rmy.android.http_shortcuts.components.ToolbarIcon
import ch.rmy.android.http_shortcuts.components.bindViewModel

@Composable
fun CurlImportScreen() {
    val (viewModel, state) = bindViewModel<CurlImportViewState, CurlImportViewModel>()

    val inputText by viewModel.inputText.collectAsStateWithLifecycle()

    SimpleScaffold(
        viewState = state,
        title = stringResource(R.string.title_curl_import),
        backButton = BackButton.CROSS,
        actions = { viewState ->
            ToolbarIcon(
                Icons.Filled.Check,
                contentDescription = stringResource(R.string.curl_import_button),
                enabled = viewState.submitButtonEnabled,
                onClick = viewModel::onSubmitButtonClicked,
            )
        },
    ) {
        CurlImportContent(
            inputText = inputText,
            unsupportedOptions = state?.unsupportedOptions ?: emptyList(),
            onInputTextChanged = viewModel::onInputTextChanged,
            onSubmit = viewModel::onSubmitButtonClicked,
        )
    }
}

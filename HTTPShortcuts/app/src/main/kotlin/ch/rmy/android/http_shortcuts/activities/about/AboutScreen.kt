package ch.rmy.android.http_shortcuts.activities.about

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.ChangeLogDialog
import ch.rmy.android.http_shortcuts.components.SimpleScaffold
import ch.rmy.android.http_shortcuts.components.bindViewModel

@Composable
fun AboutScreen() {
    val (viewModel, state) = bindViewModel<AboutViewState, AboutViewModel>()

    SimpleScaffold(
        viewState = state,
        title = stringResource(R.string.title_about),
    ) { viewState ->
        AboutContent(
            versionNumber = viewState.versionNumber,
            fDroidVisible = viewState.fDroidVisible,
            onChangeLogButtonClicked = viewModel::onChangeLogButtonClicked,
            onDocumentationButtonClicked = viewModel::onDocumentationButtonClicked,
            onContactButtonClicked = viewModel::onContactButtonClicked,
            onTranslateButtonClicked = viewModel::onTranslateButtonClicked,
            onPlayStoreButtonClicked = viewModel::onPlayStoreButtonClicked,
            onFDroidButtonClicked = viewModel::onFDroidButtonClicked,
            onGitHubButtonClicked = viewModel::onGitHubButtonClicked,
            onDonateButtonClicked = viewModel::onDonateButtonClicked,
            onAcknowledgementButtonClicked = viewModel::onAcknowledgementButtonClicked,
            onPrivacyPolicyButtonClicked = viewModel::onPrivacyPolicyButtonClicked,
        )
    }

    if (state?.changeLogDialogVisible == true) {
        ChangeLogDialog(
            permanentlyHidden = state.changeLogDialogPermanentlyHidden,
            onPermanentlyHiddenChanged = viewModel::onChangeLogDialogPermanentlyHiddenChanged,
            onDismissRequested = viewModel::onDialogDismissalRequested,
        )
    }
}

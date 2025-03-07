package ch.rmy.android.http_shortcuts.activities.certpinning

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.FloatingAddButton
import ch.rmy.android.http_shortcuts.components.SimpleScaffold
import ch.rmy.android.http_shortcuts.components.ToolbarIcon
import ch.rmy.android.http_shortcuts.components.bindViewModel

@Composable
fun CertPinningScreen() {
    val (viewModel, state) = bindViewModel<CertPinningViewState, CertPinningViewModel>()

    SimpleScaffold(
        viewState = state,
        title = stringResource(R.string.title_certificate_pinning),
        floatingActionButton = {
            FloatingAddButton(
                onClick = viewModel::onCreatePinButtonClicked,
                contentDescription = stringResource(R.string.accessibility_label_add_certificate_pinning_fab),
            )
        },
        actions = {
            ToolbarIcon(
                Icons.AutoMirrored.Filled.HelpOutline,
                contentDescription = stringResource(R.string.button_show_help),
                onClick = viewModel::onHelpButtonClicked,
            )
        },
    ) { viewState ->
        CertPinningContent(
            pins = viewState.pins,
            onPinClicked = viewModel::onPinClicked,
        )
    }

    CertPinningDialogs(
        dialogState = state?.dialogState,
        onEditConfirmed = viewModel::onEditConfirmed,
        onEditOptionSelected = viewModel::onEditOptionSelected,
        onDeleteOptionSelected = viewModel::onDeleteOptionSelected,
        onDeletionConfirmed = viewModel::onDeletionConfirmed,
        onDismissed = viewModel::onDialogDismissed,
    )
}

package ch.rmy.android.http_shortcuts.activities.troubleshooting

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.SimpleScaffold
import ch.rmy.android.http_shortcuts.components.bindViewModel

@Composable
fun TroubleShootingScreen() {
    val (viewModel, state) = bindViewModel<TroubleShootingViewState, TroubleShootingViewModel>()

    SimpleScaffold(
        viewState = state,
        title = stringResource(R.string.settings_troubleshooting),
    ) { viewState ->
        TroubleShootingContent(
            batteryOptimizationButtonVisible = viewState.batteryOptimizationButtonVisible,
            allowXiaomiOverlayButtonVisible = viewState.allowXiaomiOverlayButtonVisible,
            onEventHistoryClicked = viewModel::onEventHistoryClicked,
            onClearCookiesButtonClicked = viewModel::onClearCookiesButtonClicked,
            onCancelAllPendingExecutionsButtonClicked = viewModel::onCancelAllPendingExecutionsButtonClicked,
            onAllowOverlayButtonClicked = viewModel::onAllowOverlayButtonClicked,
            onAllowXiaomiOverlayButtonClicked = viewModel::onAllowXiaomiOverlayButtonClicked,
            onBatteryOptimizationButtonClicked = viewModel::onBatteryOptimizationButtonClicked,
            onDocumentationButtonClicked = viewModel::onDocumentationButtonClicked,
            onContactButtonClicked = viewModel::onContactButtonClicked,
        )
    }

    TroubleShootingDialogs(
        dialogState = state?.dialogState,
        onClearCookiesConfirmed = viewModel::onClearCookiesConfirmed,
        onDismissalRequested = viewModel::onDialogDismissalRequested,
    )
}

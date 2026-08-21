package ch.rmy.android.http_shortcuts.activities.troubleshooting

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.SimpleScaffold
import ch.rmy.android.http_shortcuts.components.bindViewModel

@Composable
fun TroubleShootingScreen() {
    val (viewModel, state) = bindViewModel<TroubleShootingViewState, TroubleShootingViewModel>()

    val localNetworkPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            viewModel.onAllowLocalNetworkAccessChanged()
        }
    }

    SimpleScaffold(
        viewState = state,
        title = stringResource(R.string.settings_troubleshooting),
    ) { viewState ->
        TroubleShootingContent(
            batteryOptimizationButtonEnabled = viewState.batteryOptimizationButtonEnabled,
            localNetworkAccessAllowed = viewState.localNetworkAccessAllowed,
            allowXiaomiOverlayButtonVisible = viewState.allowXiaomiOverlayButtonVisible,
            performanceOptimizationsEnabled = viewState.performanceOptimizationsEnabled,
            onEventHistoryClicked = viewModel::onEventHistoryClicked,
            onClearCookiesButtonClicked = viewModel::onClearCookiesButtonClicked,
            onCancelAllPendingExecutionsButtonClicked = viewModel::onCancelAllPendingExecutionsButtonClicked,
            onAllowOverlayButtonClicked = viewModel::onAllowOverlayButtonClicked,
            onAllowXiaomiOverlayButtonClicked = viewModel::onAllowXiaomiOverlayButtonClicked,
            onBatteryOptimizationButtonClicked = viewModel::onBatteryOptimizationButtonClicked,
            onPerformanceOptimizationsChanged = viewModel::onPerformanceOptimizationsChanged,
            onDocumentationButtonClicked = viewModel::onDocumentationButtonClicked,
            onContactButtonClicked = viewModel::onContactButtonClicked,
            onAllowLocalNetworkAccessClicked = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN) {
                    localNetworkPermissionLauncher.launch(Manifest.permission.ACCESS_LOCAL_NETWORK)
                }
            },
        )
    }

    TroubleShootingDialogs(
        dialogState = state?.dialogState,
        onClearCookiesConfirmed = viewModel::onClearCookiesConfirmed,
        onDismissalRequested = viewModel::onDialogDismissalRequested,
    )
}

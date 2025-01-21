package ch.rmy.android.http_shortcuts.activities.editor.executionsettings

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ch.rmy.android.framework.extensions.consume
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.EventHandler
import ch.rmy.android.http_shortcuts.components.SimpleScaffold
import ch.rmy.android.http_shortcuts.components.bindViewModel

@Composable
fun ExecutionSettingsScreen() {
    val (viewModel, state) = bindViewModel<ExecutionSettingsViewState, ExecutionSettingsViewModel>()

    val requestPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = {},
    )

    BackHandler(state != null) {
        viewModel.onBackPressed()
    }

    EventHandler { event ->
        when (event) {
            is ExecutionSettingsEvent.RequestNotificationPermission -> consume {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requestPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
            else -> false
        }
    }

    SimpleScaffold(
        viewState = state,
        title = stringResource(R.string.label_execution_settings),
    ) { viewState ->
        ExecutionSettingsContent(
            runInBackground = viewState.runInBackground,
            delay = viewState.delay,
            waitForConnection = viewState.waitForConnection,
            waitForConnectionOptionVisible = viewState.waitForConnectionOptionVisible,
            confirmationType = viewState.confirmationType,
            directShareOptionVisible = viewState.directShareOptionVisible,
            launcherShortcut = viewState.launcherShortcut,
            secondaryLauncherShortcut = viewState.secondaryLauncherShortcut,
            quickSettingsTileShortcut = viewState.quickSettingsTileShortcut,
            excludeFromHistory = viewState.excludeFromHistory,
            repetitionInterval = viewState.repetitionInterval,
            canUseBiometrics = viewState.canUseBiometrics,
            excludeFromFileSharing = viewState.excludeFromFileSharing,
            canUseFiles = viewState.canUseFiles,
            usesFiles = viewState.usesFiles,
            onRunInBackgroundChanged = viewModel::onRunInBackgroundChanged,
            onLauncherShortcutChanged = viewModel::onLauncherShortcutChanged,
            onSecondaryLauncherShortcutChanged = viewModel::onSecondaryLauncherShortcutChanged,
            onQuickSettingsTileShortcutChanged = viewModel::onQuickSettingsTileShortcutChanged,
            onExcludeFromHistoryChanged = viewModel::onExcludeFromHistoryChanged,
            onConfirmationTypeChanged = viewModel::onConfirmationTypeChanged,
            onWaitForConnectionChanged = viewModel::onWaitForConnectionChanged,
            onDelayButtonClicked = viewModel::onDelayButtonClicked,
            onRepetitionIntervalChanged = viewModel::onRepetitionIntervalChanged,
            onExcludeFromFileSharingChanged = viewModel::onExcludeFromFileSharingChanged,
        )
    }

    ExecutionSettingsDialogs(
        dialogState = state?.dialogState,
        onConfirmAppOverlay = viewModel::onAppOverlayDialogConfirmed,
        onConfirmDelay = viewModel::onDelayChanged,
        onRunInBackgroundInfoDismissed = viewModel::onRunInBackgroundInfoDismissed,
        onDismissed = viewModel::onDismissDialog,
    )
}

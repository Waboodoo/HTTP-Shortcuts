package ch.rmy.android.http_shortcuts.activities.editor.executionsettings

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.SimpleScaffold
import ch.rmy.android.http_shortcuts.components.bindViewModel

@Composable
fun ExecutionSettingsScreen() {
    val (viewModel, state) = bindViewModel<ExecutionSettingsViewState, ExecutionSettingsViewModel>()

    BackHandler(state != null) {
        viewModel.onBackPressed()
    }

    SimpleScaffold(
        viewState = state,
        title = stringResource(R.string.label_execution_settings),
    ) { viewState ->
        ExecutionSettingsContent(
            delay = viewState.delay,
            waitForConnection = viewState.waitForConnection,
            waitForConnectionOptionVisible = viewState.waitForConnectionOptionVisible,
            confirmationType = viewState.confirmationType,
            launcherShortcutOptionVisible = viewState.launcherShortcutOptionVisible,
            directShareOptionVisible = viewState.directShareOptionVisible,
            launcherShortcut = viewState.launcherShortcut,
            secondaryLauncherShortcut = viewState.secondaryLauncherShortcut,
            quickSettingsTileShortcutOptionVisible = viewState.quickSettingsTileShortcutOptionVisible,
            quickSettingsTileShortcut = viewState.quickSettingsTileShortcut,
            excludeFromHistory = viewState.excludeFromHistory,
            repetitionInterval = viewState.repetitionInterval,
            canUseBiometrics = viewState.canUseBiometrics,
            excludeFromFileSharing = viewState.excludeFromFileSharing,
            usesFiles = viewState.usesFiles,
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
        onDismissed = viewModel::onDismissDialog,
    )
}

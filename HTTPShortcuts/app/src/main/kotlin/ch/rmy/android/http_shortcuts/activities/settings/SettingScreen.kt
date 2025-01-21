package ch.rmy.android.http_shortcuts.activities.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.SimpleScaffold
import ch.rmy.android.http_shortcuts.components.bindViewModel

@Composable
fun SettingsScreen() {
    val (viewModel, state) = bindViewModel<SettingsViewState, SettingsViewModel>()

    SimpleScaffold(
        viewState = state,
        title = stringResource(R.string.title_settings),
    ) { viewState ->
        SettingsContent(
            privacySectionVisible = viewState.privacySectionVisible,
            quickSettingsTileButtonVisible = viewState.quickSettingsTileButtonVisible,
            selectedLanguage = viewState.selectedLanguage,
            selectedDarkModeOption = viewState.selectedDarkModeOption,
            selectedClickActionOption = viewState.selectedClickActionOption,
            crashReportingEnabled = viewState.crashReportingAllowed,
            deviceId = viewState.deviceId,
            colorTheme = viewState.colorTheme,
            showHiddenShortcuts = viewState.showHiddenShortcuts,
            onLanguageSelected = viewModel::onLanguageSelected,
            onDarkModeOptionSelected = viewModel::onDarkModeOptionSelected,
            onClickActionOptionSelected = viewModel::onClickActionOptionSelected,
            onChangeTitleButtonClicked = viewModel::onChangeTitleButtonClicked,
            onUserAgentButtonClicked = viewModel::onUserAgentButtonClicked,
            onLockButtonClicked = viewModel::onLockButtonClicked,
            onQuickSettingsTileButtonClicked = viewModel::onQuickSettingsTileButtonClicked,
            onCertificatePinningButtonClicked = viewModel::onCertificatePinningButtonClicked,
            onGlobalScriptingButtonClicked = viewModel::onGlobalScriptingButtonClicked,
            onCrashReportingChanged = viewModel::onCrashReportingChanged,
            onDeviceIdButtonClicked = viewModel::onDeviceIdButtonClicked,
            onColorThemeChanged = viewModel::onColorThemeChanged,
            onShowHiddenShortcutsChanged = viewModel::onShowHiddenShortcutsChanged,
        )
    }

    SettingsDialogs(
        dialogState = state?.dialogState,
        onClearCookiesConfirmed = viewModel::onClearCookiesConfirmed,
        onLockConfirmed = viewModel::onLockConfirmed,
        onTitleChangeConfirmed = viewModel::onTitleChangeConfirmed,
        onUserAgentChangeConfirmed = viewModel::onUserAgentChangeConfirmed,
        onDismissalRequested = viewModel::onDialogDismissalRequested,
    )
}

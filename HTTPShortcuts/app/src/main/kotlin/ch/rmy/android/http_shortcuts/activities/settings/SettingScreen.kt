package ch.rmy.android.http_shortcuts.activities.settings

import android.Manifest
import android.os.Build
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
fun SettingsScreen() {
    val (viewModel, state) = bindViewModel<SettingsViewState, SettingsViewModel>()

    val requestNotificationPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = viewModel::onMediaControlsNotificationPermissionResult,
    )

    EventHandler { event ->
        when (event) {
            is SettingsEvent.RequestNotificationPermission -> consume {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    viewModel.onMediaControlsNotificationPermissionResult(true)
                }
            }
            else -> false
        }
    }

    SimpleScaffold(
        viewState = state,
        title = stringResource(R.string.title_settings),
    ) { viewState ->
        SettingsContent(
            isInSyncReplaceMode = viewState.isInSyncReplaceMode,
            privacySectionVisible = viewState.privacySectionVisible,
            quickSettingsTileButtonVisible = viewState.quickSettingsTileButtonVisible,
            selectedLanguage = viewState.selectedLanguage,
            hasLock = viewState.hasLock,
            selectedDarkModeOption = viewState.selectedDarkModeOption,
            selectedClickActionOption = viewState.selectedClickActionOption,
            crashReportingEnabled = viewState.crashReportingAllowed,
            colorTheme = viewState.colorTheme,
            showHiddenShortcuts = viewState.showHiddenShortcuts,
            mediaControlsEnabled = viewState.mediaControlsEnabled,
            mediaControlCategories = viewState.mediaControlCategories,
            mediaControlsIncludeAllCategories = viewState.mediaControlsIncludeAllCategories,
            mediaControlSelectedCategoryIds = viewState.mediaControlSelectedCategoryIds,
            rememberActiveCategory = viewState.rememberActiveCategory,
            rememberActiveCategoryEnabled = viewState.rememberActiveCategoryEnabled,
            translationProgress = viewState.translationProgress,
            browserPackageNameOptions = viewState.browserPackageNameOptions,
            defaultBrowser = viewState.defaultBrowser,
            onLanguageSelected = viewModel::onLanguageSelected,
            onDarkModeOptionSelected = viewModel::onDarkModeOptionSelected,
            onClickActionOptionSelected = viewModel::onClickActionOptionSelected,
            onChangeTitleButtonClicked = viewModel::onChangeTitleButtonClicked,
            onChangeAppIconButtonClicked = viewModel::onChangeAppIconButtonClicked,
            onUserAgentButtonClicked = viewModel::onUserAgentButtonClicked,
            onLockButtonClicked = viewModel::onLockButtonClicked,
            onQuickSettingsTileButtonClicked = viewModel::onQuickSettingsTileButtonClicked,
            onCertificatePinningButtonClicked = viewModel::onCertificatePinningButtonClicked,
            onGlobalScriptingButtonClicked = viewModel::onGlobalScriptingButtonClicked,
            onCrashReportingChanged = viewModel::onCrashReportingChanged,
            onColorThemeChanged = viewModel::onColorThemeChanged,
            onShowHiddenShortcutsChanged = viewModel::onShowHiddenShortcutsChanged,
            onMediaControlsChanged = viewModel::onMediaControlsChanged,
            onMediaControlCategoriesButtonClicked = viewModel::onMediaControlCategoriesButtonClicked,
            onMediaControlInfoButtonClicked = viewModel::onMediaControlInfoButtonClicked,
            onRememberActiveCategoryChanged = viewModel::onRememberActiveCategoryChanged,
            onTranslateButtonClicked = viewModel::onTranslateButtonClicked,
            onDefaultBrowserChanged = viewModel::onDefaultBrowserChanged,
        )
    }

    SettingsDialogs(
        dialogState = state?.dialogState,
        onClearCookiesConfirmed = viewModel::onClearCookiesConfirmed,
        onLockConfirmed = viewModel::onLockConfirmed,
        onUnlockDialogSubmitted = viewModel::onUnlockDialogSubmitted,
        onLockRemoved = viewModel::onLockRemoved,
        onTitleChangeConfirmed = viewModel::onTitleChangeConfirmed,
        onUserAgentChangeConfirmed = viewModel::onUserAgentChangeConfirmed,
        onAppIconTypeSelected = viewModel::onAppIconTypeSelected,
        onMediaControlCategoriesSelected = viewModel::onMediaControlCategoriesSelected,
        onDismissalRequested = viewModel::onDialogDismissalRequested,
    )
}

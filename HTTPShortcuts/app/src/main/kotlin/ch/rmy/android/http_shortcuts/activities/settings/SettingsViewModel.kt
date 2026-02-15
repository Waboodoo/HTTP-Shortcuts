package ch.rmy.android.http_shortcuts.activities.settings

import android.app.Application
import androidx.lifecycle.viewModelScope
import ch.rmy.android.framework.extensions.context
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.main.usecases.UnlockAppUseCase
import ch.rmy.android.http_shortcuts.activities.settings.usecases.CreateQuickSettingsTileUseCase
import ch.rmy.android.http_shortcuts.applock.AppLockController
import ch.rmy.android.http_shortcuts.data.domains.app_config.AppConfigRepository
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryRepository
import ch.rmy.android.http_shortcuts.data.enums.ShortcutClickBehavior
import ch.rmy.android.http_shortcuts.data.settings.DeviceLocalPreferences
import ch.rmy.android.http_shortcuts.data.settings.UserPreferences
import ch.rmy.android.http_shortcuts.http.CookieManager
import ch.rmy.android.http_shortcuts.logging.Logging
import ch.rmy.android.http_shortcuts.navigation.NavigationDestination
import ch.rmy.android.http_shortcuts.sync.ObserveSyncReplaceUseCase
import ch.rmy.android.http_shortcuts.utils.BiometricUtil
import ch.rmy.android.http_shortcuts.utils.DarkThemeHelper
import ch.rmy.android.http_shortcuts.utils.LocaleHelper
import ch.rmy.android.http_shortcuts.utils.RestrictionsUtil
import ch.rmy.android.http_shortcuts.utils.UserAgentProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel
@Inject
constructor(
    application: Application,
    private val userPreferences: UserPreferences,
    private val categoryRepository: CategoryRepository,
    private val appConfigRepository: AppConfigRepository,
    private val appLockController: AppLockController,
    private val localeHelper: LocaleHelper,
    private val cookieManager: CookieManager,
    private val restrictionsUtil: RestrictionsUtil,
    private val createQuickSettingsTile: CreateQuickSettingsTileUseCase,
    private val biometricUtil: BiometricUtil,
    private val deviceLocalPreferences: DeviceLocalPreferences,
    private val unlockApp: UnlockAppUseCase,
    private val observeSyncReplace: ObserveSyncReplaceUseCase,
) : BaseViewModel<Unit, SettingsViewState>(application) {

    override suspend fun initialize(data: Unit): SettingsViewState {
        val hasLockFlow = appLockController.observeLock().map { it != null }
        val hasLock = hasLockFlow.first()
        viewModelScope.launch {
            hasLockFlow
                .distinctUntilChanged()
                .drop(1)
                .collect { hasLock ->
                    updateViewState {
                        copy(hasLock = hasLock)
                    }
                    if (!hasLock) {
                        showSnackbar(R.string.message_app_lock_removed)
                    }
                }
        }

        val isInSyncReplaceMode = monitorFlow(observeSyncReplace()) { isInSyncReplaceMode ->
            updateViewState {
                copy(
                    isInSyncReplaceMode = isInSyncReplaceMode,
                )
            }
        }

        return SettingsViewState(
            privacySectionVisible = Logging.supportsCrashReporting,
            quickSettingsTileButtonVisible = restrictionsUtil.canCreateQuickSettingsTiles(),
            selectedLanguage = userPreferences.language,
            hasLock = hasLock,
            selectedDarkModeOption = userPreferences.darkThemeSetting,
            selectedClickActionOption = userPreferences.clickBehavior,
            crashReportingAllowed = userPreferences.isCrashReportingAllowed,
            colorTheme = userPreferences.colorTheme,
            showHiddenShortcuts = userPreferences.showHiddenShortcuts,
            rememberActiveCategory = userPreferences.isRememberActiveCategory,
            rememberActiveCategoryEnabled = categoryRepository.getCategories().count { !it.hidden } > 1,
            isInSyncReplaceMode = isInSyncReplaceMode,
        )
    }

    fun onLockButtonClicked() = runAction {
        val appLock = appLockController.getLock()

        if (appLock != null) {
            withProgressTracking {
                if (appLockController.getTimeSinceLastUnlock()?.let { it < 2.minutes } == true) {
                    updateDialogState(
                        SettingsDialogState.LockApp(
                            canUseBiometrics = biometricUtil.canUseBiometrics(),
                            hasLock = true,
                            usesBiometrics = appLock.useBiometrics,
                        ),
                    )
                } else {
                    unlockApp(
                        showPasswordDialog = {
                            runAction {
                                appLockController.unlock()
                                updateDialogState(
                                    SettingsDialogState.Unlock(),
                                )
                            }
                        },
                        onSuccess = {
                            runAction {
                                appLockController.unlock()
                                updateDialogState(
                                    SettingsDialogState.LockApp(
                                        canUseBiometrics = biometricUtil.canUseBiometrics(),
                                        hasLock = true,
                                        usesBiometrics = appLock.useBiometrics,
                                    ),
                                )
                            }
                        },
                    )
                }
            }
        } else {
            updateDialogState(
                SettingsDialogState.LockApp(
                    canUseBiometrics = biometricUtil.canUseBiometrics(),
                    hasLock = false,
                    usesBiometrics = true,
                ),
            )
        }
    }

    fun onUnlockDialogSubmitted(password: String) = runAction {
        withProgressTracking {
            if (appLockController.isPasswordCorrect(password)) {
                val appLock = appLockController.getLock()
                updateDialogState(
                    SettingsDialogState.LockApp(
                        canUseBiometrics = biometricUtil.canUseBiometrics(),
                        hasLock = true,
                        usesBiometrics = appLock?.useBiometrics == true,
                    ),
                )
            } else {
                updateDialogState(SettingsDialogState.Progress)
                delay(Random.nextInt(from = 1000, until = 5000).milliseconds)
                updateDialogState(SettingsDialogState.Unlock(tryAgain = true))
            }
        }
    }

    fun onLockConfirmed(password: String, useBiometrics: Boolean) = runAction {
        updateDialogState(null)
        withProgressTracking {
            appLockController.setLock(password, useBiometrics)
            closeScreen()
        }
    }

    fun onLockRemoved() = runAction {
        updateDialogState(null)
        withProgressTracking {
            appLockController.removeLock()
        }
    }

    fun onClearCookiesConfirmed() = runAction {
        updateDialogState(null)
        launch(Dispatchers.IO) {
            cookieManager.clearCookies()
        }
        showSnackbar(R.string.message_cookies_cleared)
    }

    fun onTitleChangeConfirmed(newTitle: String) = runAction {
        updateDialogState(null)
        appConfigRepository.setToolbarTitle(newTitle)
        showSnackbar(R.string.message_title_changed)
    }

    fun onUserAgentChangeConfirmed(newUserAgent: String) = runAction {
        updateDialogState(null)
        userPreferences.userAgent = newUserAgent
        showSnackbar(R.string.message_user_agent_changed)
    }

    fun onQuickSettingsTileButtonClicked() = runAction {
        val success = createQuickSettingsTile()
        if (success) {
            showSnackbar(R.string.message_quick_settings_tile_added)
        }
    }

    fun onLanguageSelected(language: String?) = runAction {
        userPreferences.language = language
        updateViewState {
            copy(selectedLanguage = language)
        }
        localeHelper.applyLocale(language)
    }

    fun onDarkModeOptionSelected(option: String) = runAction {
        userPreferences.darkThemeSetting = option
        updateViewState {
            copy(selectedDarkModeOption = option)
        }
        DarkThemeHelper.applyDarkThemeSettings(option)
    }

    fun onClickActionOptionSelected(option: ShortcutClickBehavior) = runAction {
        userPreferences.clickBehavior = option
        updateViewState {
            copy(selectedClickActionOption = option)
        }
    }

    fun onChangeTitleButtonClicked() = runAction {
        val oldTitle = appConfigRepository.getToolbarTitle()
        updateDialogState(SettingsDialogState.ChangeTitle(oldTitle))
    }

    fun onUserAgentButtonClicked() = runAction {
        updateDialogState(
            SettingsDialogState.ChangeUserAgent(
                oldUserAgent = userPreferences.userAgent ?: "",
                placeholder = UserAgentProvider.getDefaultUserAgent(),
            ),
        )
    }

    fun onCertificatePinningButtonClicked() = runAction {
        navigate(NavigationDestination.CertPinning)
    }

    fun onGlobalScriptingButtonClicked() = runAction {
        navigate(NavigationDestination.GlobalScripting)
    }

    fun onCrashReportingChanged(allowed: Boolean) = runAction {
        updateViewState {
            copy(crashReportingAllowed = allowed)
        }
        userPreferences.isCrashReportingAllowed = allowed
        if (!allowed) {
            Logging.disableCrashReporting(context)
        }
    }

    fun onDialogDismissalRequested() = runAction {
        updateDialogState(null)
    }

    private suspend fun updateDialogState(dialogState: SettingsDialogState?) {
        updateViewState {
            copy(dialogState = dialogState)
        }
    }

    fun onColorThemeChanged(colorTheme: String) = runAction {
        userPreferences.colorTheme = colorTheme
        updateViewState {
            copy(colorTheme = userPreferences.colorTheme)
        }
    }

    fun onShowHiddenShortcutsChanged(show: Boolean) = runAction {
        userPreferences.showHiddenShortcuts = show
        updateViewState {
            copy(showHiddenShortcuts = show)
        }
    }

    fun onRememberActiveCategoryChanged(remember: Boolean) = runAction {
        userPreferences.isRememberActiveCategory = remember
        if (!remember) {
            deviceLocalPreferences.lastActiveCategoryId = null
        }
        updateViewState {
            copy(rememberActiveCategory = remember)
        }
    }
}

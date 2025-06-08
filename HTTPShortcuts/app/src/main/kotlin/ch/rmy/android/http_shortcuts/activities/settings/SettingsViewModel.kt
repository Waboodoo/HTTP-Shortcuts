package ch.rmy.android.http_shortcuts.activities.settings

import android.app.Application
import ch.rmy.android.framework.extensions.context
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.settings.usecases.CreateQuickSettingsTileUseCase
import ch.rmy.android.http_shortcuts.data.domains.app_config.AppConfigRepository
import ch.rmy.android.http_shortcuts.data.domains.app_lock.AppLockRepository
import ch.rmy.android.http_shortcuts.data.enums.ShortcutClickBehavior
import ch.rmy.android.http_shortcuts.data.settings.UserPreferences
import ch.rmy.android.http_shortcuts.http.CookieManager
import ch.rmy.android.http_shortcuts.logging.Logging
import ch.rmy.android.http_shortcuts.navigation.NavigationDestination
import ch.rmy.android.http_shortcuts.navigation.NavigationDestination.Settings.RESULT_APP_LOCKED
import ch.rmy.android.http_shortcuts.utils.BiometricUtil
import ch.rmy.android.http_shortcuts.utils.DarkThemeHelper
import ch.rmy.android.http_shortcuts.utils.LocaleHelper
import ch.rmy.android.http_shortcuts.utils.RestrictionsUtil
import ch.rmy.android.http_shortcuts.utils.UserAgentProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.mindrot.jbcrypt.BCrypt

@HiltViewModel
class SettingsViewModel
@Inject
constructor(
    application: Application,
    private val userPreferences: UserPreferences,
    private val appConfigRepository: AppConfigRepository,
    private val appLockRepository: AppLockRepository,
    private val localeHelper: LocaleHelper,
    private val cookieManager: CookieManager,
    private val restrictionsUtil: RestrictionsUtil,
    private val createQuickSettingsTile: CreateQuickSettingsTileUseCase,
    private val biometricUtil: BiometricUtil,
) : BaseViewModel<Unit, SettingsViewState>(application) {

    override suspend fun initialize(data: Unit) = SettingsViewState(
        privacySectionVisible = Logging.supportsCrashReporting,
        quickSettingsTileButtonVisible = restrictionsUtil.canCreateQuickSettingsTiles(),
        selectedLanguage = userPreferences.language,
        selectedDarkModeOption = userPreferences.darkThemeSetting,
        selectedClickActionOption = userPreferences.clickBehavior,
        crashReportingAllowed = userPreferences.isCrashReportingAllowed,
        colorTheme = userPreferences.colorTheme,
        showHiddenShortcuts = userPreferences.showHiddenShortcuts,
    )

    fun onLockButtonClicked() = runAction {
        updateDialogState(SettingsDialogState.LockApp(canUseBiometrics = biometricUtil.canUseBiometrics()))
    }

    fun onLockConfirmed(password: String, useBiometrics: Boolean) = runAction {
        updateDialogState(null)
        withProgressTracking {
            try {
                appLockRepository.setLock(BCrypt.hashpw(password, BCrypt.gensalt()), useBiometrics)
                closeScreen(result = RESULT_APP_LOCKED)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                showSnackbar(R.string.error_generic, long = true)
                logException(e)
            }
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
}

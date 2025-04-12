package ch.rmy.android.http_shortcuts.activities.settings

import android.app.Application
import ch.rmy.android.framework.extensions.context
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.utils.ClipboardUtil
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.settings.usecases.CreateQuickSettingsTileUseCase
import ch.rmy.android.http_shortcuts.data.domains.app_config.AppConfigRepository
import ch.rmy.android.http_shortcuts.data.domains.app_lock.AppLockRepository
import ch.rmy.android.http_shortcuts.data.enums.ShortcutClickBehavior
import ch.rmy.android.http_shortcuts.http.CookieManager
import ch.rmy.android.http_shortcuts.logging.Logging
import ch.rmy.android.http_shortcuts.navigation.NavigationDestination
import ch.rmy.android.http_shortcuts.navigation.NavigationDestination.Settings.RESULT_APP_LOCKED
import ch.rmy.android.http_shortcuts.utils.BiometricUtil
import ch.rmy.android.http_shortcuts.utils.DarkThemeHelper
import ch.rmy.android.http_shortcuts.utils.LocaleHelper
import ch.rmy.android.http_shortcuts.utils.RestrictionsUtil
import ch.rmy.android.http_shortcuts.utils.Settings
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
    private val settings: Settings,
    private val appConfigRepository: AppConfigRepository,
    private val appLockRepository: AppLockRepository,
    private val localeHelper: LocaleHelper,
    private val cookieManager: CookieManager,
    private val restrictionsUtil: RestrictionsUtil,
    private val createQuickSettingsTile: CreateQuickSettingsTileUseCase,
    private val biometricUtil: BiometricUtil,
    private val clipboardUtil: ClipboardUtil,
) : BaseViewModel<Unit, SettingsViewState>(application) {

    override suspend fun initialize(data: Unit) = SettingsViewState(
        privacySectionVisible = Logging.supportsCrashReporting,
        quickSettingsTileButtonVisible = restrictionsUtil.canCreateQuickSettingsTiles(),
        selectedLanguage = settings.language,
        selectedDarkModeOption = settings.darkThemeSetting,
        selectedClickActionOption = settings.clickBehavior,
        crashReportingAllowed = settings.isCrashReportingAllowed,
        deviceId = settings.deviceId,
        colorTheme = settings.colorTheme,
        showHiddenShortcuts = settings.showHiddenShortcuts,
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
        settings.userAgent = newUserAgent
        showSnackbar(R.string.message_user_agent_changed)
    }

    fun onQuickSettingsTileButtonClicked() = runAction {
        val success = createQuickSettingsTile()
        if (success) {
            showSnackbar(R.string.message_quick_settings_tile_added)
        }
    }

    fun onLanguageSelected(language: String?) = runAction {
        settings.language = language
        updateViewState {
            copy(selectedLanguage = language)
        }
        localeHelper.applyLocale(language)
    }

    fun onDarkModeOptionSelected(option: String) = runAction {
        settings.darkThemeSetting = option
        updateViewState {
            copy(selectedDarkModeOption = option)
        }
        DarkThemeHelper.applyDarkThemeSettings(option)
    }

    fun onClickActionOptionSelected(option: ShortcutClickBehavior) = runAction {
        settings.clickBehavior = option
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
            SettingsDialogState.ChangeUserAgent(settings.userAgent ?: ""),
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
        settings.isCrashReportingAllowed = allowed
        if (!allowed) {
            Logging.disableCrashReporting(context)
        }
    }

    fun onDeviceIdButtonClicked() = runAction {
        clipboardUtil.copyToClipboard(settings.deviceId)
        showSnackbar(R.string.message_device_id_copied)
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
        settings.colorTheme = colorTheme
        updateViewState {
            copy(colorTheme = settings.colorTheme)
        }
    }

    fun onShowHiddenShortcutsChanged(show: Boolean) = runAction {
        settings.showHiddenShortcuts = show
        updateViewState {
            copy(showHiddenShortcuts = show)
        }
    }
}

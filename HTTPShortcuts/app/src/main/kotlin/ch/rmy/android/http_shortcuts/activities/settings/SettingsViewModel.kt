package ch.rmy.android.http_shortcuts.activities.settings

import android.app.Application
import ch.rmy.android.framework.extensions.context
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.settings.usecases.CreateQuickSettingsTileUseCase
import ch.rmy.android.http_shortcuts.data.domains.app.AppRepository
import ch.rmy.android.http_shortcuts.data.domains.pending_executions.PendingExecutionsRepository
import ch.rmy.android.http_shortcuts.data.enums.ShortcutClickBehavior
import ch.rmy.android.http_shortcuts.http.CookieManager
import ch.rmy.android.http_shortcuts.logging.Logging
import ch.rmy.android.http_shortcuts.navigation.NavigationDestination
import ch.rmy.android.http_shortcuts.navigation.NavigationDestination.Settings.RESULT_APP_LOCKED
import ch.rmy.android.http_shortcuts.utils.AppOverlayUtil
import ch.rmy.android.http_shortcuts.utils.BiometricUtil
import ch.rmy.android.http_shortcuts.utils.DarkThemeHelper
import ch.rmy.android.http_shortcuts.utils.LocaleHelper
import ch.rmy.android.http_shortcuts.utils.RestrictionsUtil
import ch.rmy.android.http_shortcuts.utils.Settings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.mindrot.jbcrypt.BCrypt
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel
@Inject
constructor(
    application: Application,
    private val settings: Settings,
    private val appRepository: AppRepository,
    private val pendingExecutionsRepository: PendingExecutionsRepository,
    private val localeHelper: LocaleHelper,
    private val cookieManager: CookieManager,
    private val appOverlayUtil: AppOverlayUtil,
    private val restrictionsUtil: RestrictionsUtil,
    private val createQuickSettingsTile: CreateQuickSettingsTileUseCase,
    private val biometricUtil: BiometricUtil,
) : BaseViewModel<Unit, SettingsViewState>(application) {

    override suspend fun initialize(data: Unit) = SettingsViewState(
        privacySectionVisible = Logging.supportsCrashReporting,
        quickSettingsTileButtonVisible = restrictionsUtil.canCreateQuickSettingsTiles(),
        selectedLanguage = settings.language,
        selectedDarkModeOption = settings.darkThemeSetting,
        selectedClickActionOption = settings.clickBehavior,
        crashReportingAllowed = settings.isCrashReportingAllowed,
        colorTheme = settings.colorTheme,
        batteryOptimizationButtonVisible = restrictionsUtil.run {
            canRequestIgnoreBatteryOptimization() && !isIgnoringBatteryOptimizations()
        },
        allowOverlayButtonVisible = restrictionsUtil.canAllowOverlay(),
        allowXiaomiOverlayButtonVisible = restrictionsUtil.hasPermissionEditor(),
        experimentalExecutionModeEnabled = settings.useExperimentalExecutionMode,
    )

    fun onLockButtonClicked() = runAction {
        updateDialogState(SettingsDialogState.LockApp(canUseBiometrics = biometricUtil.canUseBiometrics()))
    }

    fun onLockConfirmed(password: String, useBiometrics: Boolean) = runAction {
        updateDialogState(null)
        withProgressTracking {
            try {
                appRepository.setLock(BCrypt.hashpw(password, BCrypt.gensalt()), useBiometrics)
                closeScreen(result = RESULT_APP_LOCKED)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                showSnackbar(R.string.error_generic, long = true)
                logException(e)
            }
        }
    }

    fun onClearCookiesButtonClicked() = runAction {
        updateDialogState(SettingsDialogState.ClearCookies)
    }

    fun onCancelAllPendingExecutionsButtonClicked() = runAction {
        pendingExecutionsRepository.removeAllPendingExecutions()
        showSnackbar(R.string.message_pending_executions_cancelled)
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
        appRepository.setToolbarTitle(newTitle)
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
        val oldTitle = appRepository.getToolbarTitle()
        updateDialogState(SettingsDialogState.ChangeTitle(oldTitle))
    }

    fun onUserAgentButtonClicked() = runAction {
        updateDialogState(
            SettingsDialogState.ChangeUserAgent(settings.userAgent ?: "")
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

    fun onEventHistoryClicked() = runAction {
        navigate(NavigationDestination.History)
    }

    fun onAllowOverlayButtonClicked() = runAction {
        sendIntent(appOverlayUtil.getSettingsIntent() ?: skipAction())
    }

    fun onAllowXiaomiOverlayButtonClicked() = runAction {
        sendIntent(restrictionsUtil.getPermissionEditorIntent())
    }

    fun onBatteryOptimizationButtonClicked() = runAction {
        sendIntent(restrictionsUtil.getRequestIgnoreBatteryOptimizationIntent() ?: skipAction())
    }

    fun onDialogDismissalRequested() = runAction {
        updateDialogState(null)
    }

    private suspend fun updateDialogState(dialogState: SettingsDialogState?) {
        updateViewState {
            copy(dialogState = dialogState)
        }
    }

    fun onExperimentalExecutionModeChanged(enabled: Boolean) = runAction {
        updateViewState {
            copy(experimentalExecutionModeEnabled = enabled)
        }
        settings.useExperimentalExecutionMode = enabled
    }

    fun onExperimentalHelpTextClicked() = runAction {
        navigate(NavigationDestination.Contact)
    }

    fun onColorThemeChanged(colorTheme: String) = runAction {
        settings.colorTheme = colorTheme
        updateViewState {
            copy(colorTheme = settings.colorTheme)
        }
    }
}

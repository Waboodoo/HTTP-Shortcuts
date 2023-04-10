package ch.rmy.android.http_shortcuts.activities.settings

import android.app.Application
import androidx.lifecycle.viewModelScope
import ch.rmy.android.framework.extensions.context
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.globalcode.GlobalScriptingActivity
import ch.rmy.android.http_shortcuts.activities.settings.usecases.CreateQuickSettingsTileUseCase
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.app.AppRepository
import ch.rmy.android.http_shortcuts.data.enums.ShortcutClickBehavior
import ch.rmy.android.http_shortcuts.http.CookieManager
import ch.rmy.android.http_shortcuts.logging.Logging
import ch.rmy.android.http_shortcuts.utils.AppOverlayUtil
import ch.rmy.android.http_shortcuts.utils.DarkThemeHelper
import ch.rmy.android.http_shortcuts.utils.LocaleHelper
import ch.rmy.android.http_shortcuts.utils.RestrictionsUtil
import ch.rmy.android.http_shortcuts.utils.Settings
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mindrot.jbcrypt.BCrypt
import javax.inject.Inject

class SettingsViewModel(application: Application) : BaseViewModel<Unit, SettingsViewState>(application) {

    @Inject
    lateinit var settings: Settings

    @Inject
    lateinit var appRepository: AppRepository

    @Inject
    lateinit var localeHelper: LocaleHelper

    @Inject
    lateinit var cookieManager: CookieManager

    @Inject
    lateinit var appOverlayUtil: AppOverlayUtil

    @Inject
    lateinit var restrictionsUtil: RestrictionsUtil

    @Inject
    lateinit var createQuickSettingsTile: CreateQuickSettingsTileUseCase

    init {
        getApplicationComponent().inject(this)
    }

    override fun initViewState() = SettingsViewState(
        privacySectionVisible = Logging.supportsCrashReporting,
        quickSettingsTileButtonVisible = restrictionsUtil.canCreateQuickSettingsTiles(),
        selectedLanguage = settings.language,
        selectedDarkModeOption = settings.darkThemeSetting,
        selectedClickActionOption = settings.clickBehavior,
        crashReportingAllowed = settings.isCrashReportingAllowed,
        batteryOptimizationButtonVisible = restrictionsUtil.run {
            canRequestIgnoreBatteryOptimization() && !isIgnoringBatteryOptimizations()
        },
        allowOverlayButtonVisible = restrictionsUtil.canAllowOverlay(),
        allowXiaomiOverlayButtonVisible = restrictionsUtil.hasPermissionEditor()
    )

    fun onLockButtonClicked() {
        updateDialogState(SettingsDialogState.LockApp)
    }

    fun onLockConfirmed(password: String) {
        updateDialogState(null)
        launchWithProgressTracking {
            try {
                withContext(Dispatchers.IO) {
                    appRepository.setLock(BCrypt.hashpw(password, BCrypt.gensalt()))
                }
                finishWithOkResult(
                    SettingsActivity.OpenSettings.createResult(appLocked = true),
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                showSnackbar(R.string.error_generic, long = true)
                logException(e)
            }
        }
    }

    fun onClearCookiesButtonClicked() {
        updateDialogState(SettingsDialogState.ClearCookies)
    }

    fun onClearCookiesConfirmed() {
        updateDialogState(null)
        viewModelScope.launch(Dispatchers.IO) {
            cookieManager.clearCookies()
        }
        showSnackbar(R.string.message_cookies_cleared)
    }

    fun onTitleChangeConfirmed(newTitle: String) {
        updateDialogState(null)
        viewModelScope.launch {
            appRepository.setToolbarTitle(newTitle)
            showSnackbar(R.string.message_title_changed)
        }
    }

    fun onQuickSettingsTileButtonClicked() {
        viewModelScope.launch {
            val success = createQuickSettingsTile()
            if (success) {
                showSnackbar(R.string.message_quick_settings_tile_added)
            }
        }
    }

    fun onLanguageSelected(language: String?) {
        settings.language = language
        updateViewState {
            copy(selectedLanguage = language)
        }
        localeHelper.applyLocale(language)
    }

    fun onDarkModeOptionSelected(option: String) {
        settings.darkThemeSetting = option
        updateViewState {
            copy(selectedDarkModeOption = option)
        }
        DarkThemeHelper.applyDarkThemeSettings(option)
    }

    fun onClickActionOptionSelected(option: ShortcutClickBehavior) {
        settings.clickBehavior = option
        updateViewState {
            copy(selectedClickActionOption = option)
        }
    }

    fun onChangeTitleButtonClicked() {
        viewModelScope.launch {
            val oldTitle = appRepository.getToolbarTitle()
            updateViewState {
                copy(dialogState = SettingsDialogState.ChangeTitle(oldTitle))
            }
        }
    }

    fun onGlobalScriptingButtonClicked() {
        openActivity(GlobalScriptingActivity.IntentBuilder())
    }

    fun onCrashReportingChanged(allowed: Boolean) {
        updateViewState {
            copy(crashReportingAllowed = allowed)
        }
        settings.isCrashReportingAllowed = allowed
        if (!allowed) {
            Logging.disableCrashReporting(context)
        }
    }

    fun onAllowOverlayButtonClicked() {
        openActivity(appOverlayUtil.getSettingsIntent() ?: return)
    }

    fun onAllowXiaomiOverlayButtonClicked() {
        openActivity(restrictionsUtil.getPermissionEditorIntent())
    }

    fun onBatteryOptimizationButtonClicked() {
        openActivity(restrictionsUtil.getRequestIgnoreBatteryOptimizationIntent() ?: return)
    }

    fun onDialogDismissalRequested() {
        updateDialogState(null)
    }

    private fun updateDialogState(dialogState: SettingsDialogState?) {
        updateViewState {
            copy(dialogState = dialogState)
        }
    }
}

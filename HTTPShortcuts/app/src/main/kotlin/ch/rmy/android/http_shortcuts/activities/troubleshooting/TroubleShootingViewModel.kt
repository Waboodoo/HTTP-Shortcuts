package ch.rmy.android.http_shortcuts.activities.troubleshooting

import android.app.Application
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.data.domains.pending_executions.PendingExecutionsRepository
import ch.rmy.android.http_shortcuts.http.CookieManager
import ch.rmy.android.http_shortcuts.logging.Logging
import ch.rmy.android.http_shortcuts.navigation.NavigationDestination
import ch.rmy.android.http_shortcuts.utils.AppOverlayUtil
import ch.rmy.android.http_shortcuts.utils.ExternalURLs
import ch.rmy.android.http_shortcuts.utils.RestrictionsUtil
import ch.rmy.android.http_shortcuts.utils.Settings
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@HiltViewModel
class TroubleShootingViewModel
@Inject
constructor(
    application: Application,
    private val settings: Settings,
    private val pendingExecutionsRepository: PendingExecutionsRepository,
    private val cookieManager: CookieManager,
    private val appOverlayUtil: AppOverlayUtil,
    private val restrictionsUtil: RestrictionsUtil,
) : BaseViewModel<Unit, TroubleShootingViewState>(application) {

    override suspend fun initialize(data: Unit) = TroubleShootingViewState(
        privacySectionVisible = Logging.supportsCrashReporting,
        quickSettingsTileButtonVisible = restrictionsUtil.canCreateQuickSettingsTiles(),
        selectedLanguage = settings.language,
        selectedDarkModeOption = settings.darkThemeSetting,
        selectedClickActionOption = settings.clickBehavior,
        crashReportingAllowed = settings.isCrashReportingAllowed,
        colorTheme = settings.colorTheme,
        batteryOptimizationButtonVisible = !restrictionsUtil.isIgnoringBatteryOptimizations(),
        allowXiaomiOverlayButtonVisible = restrictionsUtil.hasPermissionEditor(),
    )

    fun onClearCookiesButtonClicked() = runAction {
        updateDialogState(TroubleShootingDialogState.ClearCookies)
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

    fun onEventHistoryClicked() = runAction {
        navigate(NavigationDestination.History)
    }

    fun onAllowOverlayButtonClicked() = runAction {
        sendIntent(appOverlayUtil.getSettingsIntent())
    }

    fun onAllowXiaomiOverlayButtonClicked() = runAction {
        sendIntent(restrictionsUtil.getPermissionEditorIntent())
    }

    fun onBatteryOptimizationButtonClicked() = runAction {
        sendIntent(restrictionsUtil.getRequestIgnoreBatteryOptimizationIntent())
    }

    fun onDocumentationButtonClicked() = runAction {
        openURL(ExternalURLs.DOCUMENTATION_PAGE)
    }

    fun onContactButtonClicked() = runAction {
        navigate(NavigationDestination.Contact)
    }

    fun onDialogDismissalRequested() = runAction {
        updateDialogState(null)
    }

    private suspend fun updateDialogState(dialogState: TroubleShootingDialogState?) {
        updateViewState {
            copy(dialogState = dialogState)
        }
    }
}

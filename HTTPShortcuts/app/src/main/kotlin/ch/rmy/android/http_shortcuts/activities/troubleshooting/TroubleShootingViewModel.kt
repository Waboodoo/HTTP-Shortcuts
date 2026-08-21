package ch.rmy.android.http_shortcuts.activities.troubleshooting

import android.app.Application
import androidx.lifecycle.application
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.data.domains.pending_executions.PendingExecutionsRepository
import ch.rmy.android.http_shortcuts.data.settings.UserPreferences
import ch.rmy.android.http_shortcuts.http.CookieManager
import ch.rmy.android.http_shortcuts.logging.Logging
import ch.rmy.android.http_shortcuts.navigation.NavigationDestination
import ch.rmy.android.http_shortcuts.utils.AppOverlayUtil
import ch.rmy.android.http_shortcuts.utils.ExternalURLs
import ch.rmy.android.http_shortcuts.utils.PermissionManager
import ch.rmy.android.http_shortcuts.utils.RestrictionsUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@HiltViewModel
class TroubleShootingViewModel
@Inject
constructor(
    application: Application,
    private val userPreferences: UserPreferences,
    private val pendingExecutionsRepository: PendingExecutionsRepository,
    private val cookieManager: CookieManager,
    private val appOverlayUtil: AppOverlayUtil,
    private val restrictionsUtil: RestrictionsUtil,
    private val permissionManager: PermissionManager,
) : BaseViewModel<Unit, TroubleShootingViewState>(application) {

    override suspend fun initialize(data: Unit) = TroubleShootingViewState(
        localNetworkAccessAllowed = permissionManager.hasLocalNetworkPermission(),
        privacySectionVisible = Logging.supportsCrashReporting,
        quickSettingsTileButtonVisible = restrictionsUtil.canCreateQuickSettingsTiles(),
        selectedLanguage = userPreferences.language,
        selectedDarkModeOption = userPreferences.darkThemeSetting,
        selectedClickActionOption = userPreferences.clickBehavior,
        crashReportingAllowed = userPreferences.isCrashReportingAllowed,
        colorTheme = userPreferences.colorTheme,
        batteryOptimizationButtonEnabled = !restrictionsUtil.isIgnoringBatteryOptimizations(),
        allowXiaomiOverlayButtonVisible = restrictionsUtil.hasPermissionEditor(),
        performanceOptimizationsEnabled = !userPreferences.isHeadlessModeDisabled,
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

    fun onPerformanceOptimizationsChanged(enabled: Boolean) = runAction {
        updateViewState {
            copy(performanceOptimizationsEnabled = enabled)
        }
        userPreferences.isHeadlessModeDisabled = !enabled
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

    fun onAllowLocalNetworkAccessChanged() = runAction {
        updateViewState {
            copy(localNetworkAccessAllowed = true)
        }
    }

    private suspend fun updateDialogState(dialogState: TroubleShootingDialogState?) {
        updateViewState {
            copy(dialogState = dialogState)
        }
    }
}

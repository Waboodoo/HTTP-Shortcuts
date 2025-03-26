package ch.rmy.android.http_shortcuts.activities.editor.executionsettings

import android.app.Application
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.activities.editor.executionsettings.ExecutionSettingsEvent.RequestNotificationPermission
import ch.rmy.android.http_shortcuts.data.domains.request_parameters.RequestParameterRepository
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.TemporaryShortcutRepository
import ch.rmy.android.http_shortcuts.data.enums.ConfirmationType
import ch.rmy.android.http_shortcuts.data.enums.ParameterType
import ch.rmy.android.http_shortcuts.data.models.Shortcut.Companion.TEMPORARY_ID
import ch.rmy.android.http_shortcuts.extensions.canUseFiles
import ch.rmy.android.http_shortcuts.extensions.canWaitForConnection
import ch.rmy.android.http_shortcuts.utils.AppOverlayUtil
import ch.rmy.android.http_shortcuts.utils.BiometricUtil
import ch.rmy.android.http_shortcuts.utils.LauncherShortcutManager
import ch.rmy.android.http_shortcuts.utils.RestrictionsUtil
import ch.rmy.android.http_shortcuts.utils.Settings
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

@HiltViewModel
class ExecutionSettingsViewModel
@Inject
constructor(
    application: Application,
    private val temporaryShortcutRepository: TemporaryShortcutRepository,
    private val requestParameterRepository: RequestParameterRepository,
    private val launcherShortcutManager: LauncherShortcutManager,
    private val restrictionsUtil: RestrictionsUtil,
    private val appOverlayUtil: AppOverlayUtil,
    private val biometricUtil: BiometricUtil,
    private val settings: Settings,
) : BaseViewModel<Unit, ExecutionSettingsViewState>(application) {

    override suspend fun initialize(data: Unit): ExecutionSettingsViewState {
        val shortcut = temporaryShortcutRepository.getTemporaryShortcut()
        val hasFileParameter = if (shortcut.usesRequestParameters()) {
            requestParameterRepository.getRequestParametersByShortcutId(TEMPORARY_ID)
                .any { it.parameterType == ParameterType.FILE }
        } else {
            false
        }
        return ExecutionSettingsViewState(
            runInBackground = shortcut.runInForegroundService,
            directShareOptionVisible = launcherShortcutManager.supportsDirectShare(),
            waitForConnection = shortcut.isWaitForNetwork,
            waitForConnectionOptionVisible = shortcut.executionType.canWaitForConnection,
            launcherShortcut = shortcut.launcherShortcut,
            secondaryLauncherShortcut = shortcut.secondaryLauncherShortcut,
            quickSettingsTileShortcut = shortcut.quickSettingsTileShortcut,
            delay = shortcut.delay.milliseconds,
            confirmationType = shortcut.confirmationType,
            excludeFromHistory = shortcut.excludeFromHistory,
            repetitionInterval = shortcut.repetitionInterval,
            canUseBiometrics = biometricUtil.canUseBiometrics(),
            excludeFromFileSharing = shortcut.excludeFromFileSharing,
            canUseFiles = shortcut.executionType.canUseFiles,
            usesFiles = shortcut.usesGenericFileBody() || hasFileParameter,
        )
    }

    fun onWaitForConnectionChanged(waitForConnection: Boolean) = runAction {
        updateViewState {
            copy(waitForConnection = waitForConnection)
        }
        withProgressTracking {
            temporaryShortcutRepository.setWaitForConnection(waitForConnection)
        }
    }

    fun onExcludeFromHistoryChanged(excludeFromHistory: Boolean) = runAction {
        updateViewState {
            copy(excludeFromHistory = excludeFromHistory)
        }
        withProgressTracking {
            temporaryShortcutRepository.setExcludeFromHistory(excludeFromHistory)
        }
    }

    fun onRunInBackgroundChanged(runInBackground: Boolean) = runAction {
        if (runInBackground && !settings.isAwareOfRunningInBackgroundLimitations) {
            settings.isAwareOfRunningInBackgroundLimitations = true
            updateDialogState(ExecutionSettingsDialogState.RunInBackgroundInfo)
        }
        updateViewState {
            copy(runInBackground = runInBackground)
        }
        withProgressTracking {
            temporaryShortcutRepository.setRunInForegroundService(runInBackground)
        }
    }

    fun onLauncherShortcutChanged(launcherShortcut: Boolean) = runAction {
        updateViewState {
            copy(launcherShortcut = launcherShortcut)
        }
        withProgressTracking {
            temporaryShortcutRepository.setLauncherShortcut(launcherShortcut)
        }
    }

    fun onSecondaryLauncherShortcutChanged(secondaryLauncherShortcut: Boolean) = runAction {
        updateViewState {
            copy(secondaryLauncherShortcut = secondaryLauncherShortcut)
        }
        withProgressTracking {
            temporaryShortcutRepository.setSecondaryLauncherShortcut(secondaryLauncherShortcut)
        }
    }

    fun onQuickSettingsTileShortcutChanged(quickSettingsTileShortcut: Boolean) = runAction {
        updateViewState {
            copy(quickSettingsTileShortcut = quickSettingsTileShortcut)
        }
        withProgressTracking {
            temporaryShortcutRepository.setQuickSettingsTileShortcut(quickSettingsTileShortcut)
        }
    }

    fun onDelayChanged(delay: Duration) = runAction {
        updateViewState {
            copy(
                delay = delay,
                dialogState = null,
            )
        }
        withProgressTracking {
            temporaryShortcutRepository.setDelay(delay)
        }
    }

    fun onRunInBackgroundInfoDismissed() = runAction {
        updateDialogState(null)
        emitEvent(RequestNotificationPermission)
    }

    fun onConfirmationTypeChanged(confirmationType: ConfirmationType?) = runAction {
        updateViewState {
            copy(confirmationType = confirmationType)
        }
        withProgressTracking {
            temporaryShortcutRepository.setConfirmationType(confirmationType)
        }
    }

    fun onRepetitionIntervalChanged(repetitionInterval: Int?) = runAction {
        if (
            viewState.repetitionInterval == null &&
            repetitionInterval != null &&
            (!appOverlayUtil.canDrawOverlays() || !restrictionsUtil.isIgnoringBatteryOptimizations())
        ) {
            updateDialogState(ExecutionSettingsDialogState.AppOverlayPrompt)
        }
        updateViewState {
            copy(repetitionInterval = repetitionInterval)
        }
        withProgressTracking {
            temporaryShortcutRepository.setRepetitionInterval(repetitionInterval?.minutes)
        }
    }

    fun onExcludeFromFileSharingChanged(exclude: Boolean) = runAction {
        updateViewState {
            copy(excludeFromFileSharing = exclude)
        }
        withProgressTracking {
            temporaryShortcutRepository.setExcludeFromFileSharingChanged(exclude)
        }
    }

    fun onAppOverlayDialogConfirmed() = runAction {
        updateDialogState(null)
        sendIntent(appOverlayUtil.getSettingsIntent())
    }

    fun onDelayButtonClicked() = runAction {
        updateDialogState(
            ExecutionSettingsDialogState.DelayPicker(viewState.delay),
        )
    }

    fun onBackPressed() = runAction {
        waitForOperationsToFinish()
        closeScreen()
    }

    fun onDismissDialog() = runAction {
        updateDialogState(null)
    }

    private suspend fun updateDialogState(dialogState: ExecutionSettingsDialogState?) {
        updateViewState {
            copy(dialogState = dialogState)
        }
    }
}

package ch.rmy.android.http_shortcuts.activities.editor.executionsettings

import android.app.Application
import androidx.lifecycle.viewModelScope
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.TemporaryShortcutRepository
import ch.rmy.android.http_shortcuts.data.enums.ConfirmationType
import ch.rmy.android.http_shortcuts.data.enums.ShortcutExecutionType
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.extensions.type
import ch.rmy.android.http_shortcuts.tiles.QuickSettingsTileManager
import ch.rmy.android.http_shortcuts.utils.AppOverlayUtil
import ch.rmy.android.http_shortcuts.utils.LauncherShortcutManager
import ch.rmy.android.http_shortcuts.utils.RestrictionsUtil
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

class ExecutionSettingsViewModel(application: Application) : BaseViewModel<Unit, ExecutionSettingsViewState>(application) {

    @Inject
    lateinit var temporaryShortcutRepository: TemporaryShortcutRepository

    @Inject
    lateinit var launcherShortcutManager: LauncherShortcutManager

    @Inject
    lateinit var quickSettingsTileManager: QuickSettingsTileManager

    @Inject
    lateinit var restrictionsUtil: RestrictionsUtil

    @Inject
    lateinit var appOverlayUtil: AppOverlayUtil

    init {
        getApplicationComponent().inject(this)
    }

    private lateinit var initialViewState: ExecutionSettingsViewState

    override fun onInitializationStarted(data: Unit) {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                val temporaryShortcut = temporaryShortcutRepository.getTemporaryShortcut()
                initialViewState = createInitialViewStateFromShortcut(temporaryShortcut)
                withContext(Dispatchers.Main) {
                    finalizeInitialization()
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onInitializationError(e)
                }
            }
        }
    }

    override fun initViewState() = initialViewState

    private fun createInitialViewStateFromShortcut(shortcut: Shortcut) = ExecutionSettingsViewState(
        launcherShortcutOptionVisible = launcherShortcutManager.supportsLauncherShortcuts(),
        quickSettingsTileShortcutOptionVisible = quickSettingsTileManager.supportsQuickSettingsTiles(),
        waitForConnection = shortcut.isWaitForNetwork,
        waitForConnectionOptionVisible = shortcut.type == ShortcutExecutionType.APP,
        launcherShortcut = shortcut.launcherShortcut,
        secondaryLauncherShortcut = shortcut.secondaryLauncherShortcut,
        quickSettingsTileShortcut = shortcut.quickSettingsTileShortcut,
        delay = shortcut.delay.milliseconds,
        confirmationType = shortcut.confirmationType,
        excludeFromHistory = shortcut.excludeFromHistory,
        repetitionInterval = shortcut.repetition?.interval,
    )

    private fun onInitializationError(error: Throwable) {
        handleUnexpectedError(error)
        finish()
    }

    fun onWaitForConnectionChanged(waitForConnection: Boolean) {
        updateViewState {
            copy(waitForConnection = waitForConnection)
        }
        launchWithProgressTracking {
            temporaryShortcutRepository.setWaitForConnection(waitForConnection)
        }
    }

    fun onExcludeFromHistoryChanged(excludeFromHistory: Boolean) {
        updateViewState {
            copy(excludeFromHistory = excludeFromHistory)
        }
        launchWithProgressTracking {
            temporaryShortcutRepository.setExcludeFromHistory(excludeFromHistory)
        }
    }

    fun onLauncherShortcutChanged(launcherShortcut: Boolean) {
        updateViewState {
            copy(launcherShortcut = launcherShortcut)
        }
        launchWithProgressTracking {
            temporaryShortcutRepository.setLauncherShortcut(launcherShortcut)
        }
    }

    fun onSecondaryLauncherShortcutChanged(secondaryLauncherShortcut: Boolean) {
        updateViewState {
            copy(secondaryLauncherShortcut = secondaryLauncherShortcut)
        }
        launchWithProgressTracking {
            temporaryShortcutRepository.setSecondaryLauncherShortcut(secondaryLauncherShortcut)
        }
    }

    fun onQuickSettingsTileShortcutChanged(quickSettingsTileShortcut: Boolean) {
        updateViewState {
            copy(quickSettingsTileShortcut = quickSettingsTileShortcut)
        }
        launchWithProgressTracking {
            temporaryShortcutRepository.setQuickSettingsTileShortcut(quickSettingsTileShortcut)
        }
    }

    fun onDelayChanged(delay: Duration) {
        updateViewState {
            copy(
                delay = delay,
                dialogState = null,
            )
        }
        launchWithProgressTracking {
            temporaryShortcutRepository.setDelay(delay)
        }
    }

    fun onConfirmationTypeChanged(confirmationType: ConfirmationType?) {
        updateViewState {
            copy(confirmationType = confirmationType)
        }
        launchWithProgressTracking {
            temporaryShortcutRepository.setConfirmationType(confirmationType)
        }
    }

    fun onRepetitionIntervalChanged(repetitionInterval: Int?) {
        doWithViewState { viewState ->
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
            launchWithProgressTracking {
                temporaryShortcutRepository.setRepetitionInterval(repetitionInterval?.minutes)
            }
        }
    }

    fun onAppOverlayDialogConfirmed() {
        updateDialogState(null)
        val intent = appOverlayUtil.getSettingsIntent() ?: return
        openActivity(intent)
    }

    fun onDelayButtonClicked() {
        val delay = currentViewState?.delay ?: return
        updateDialogState(
            ExecutionSettingsDialogState.DelayPicker(delay),
        )
    }

    fun onBackPressed() {
        viewModelScope.launch {
            waitForOperationsToFinish()
            finish()
        }
    }

    fun onDismissDialog() {
        updateDialogState(null)
    }

    private fun updateDialogState(dialogState: ExecutionSettingsDialogState?) {
        updateViewState {
            copy(dialogState = dialogState)
        }
    }
}

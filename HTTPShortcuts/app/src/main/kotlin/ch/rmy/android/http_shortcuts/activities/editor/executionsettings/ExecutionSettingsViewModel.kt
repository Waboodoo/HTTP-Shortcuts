package ch.rmy.android.http_shortcuts.activities.editor.executionsettings

import android.app.Application
import androidx.lifecycle.viewModelScope
import ch.rmy.android.framework.utils.localization.DurationLocalizable
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.framework.viewmodel.WithDialog
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.activities.editor.executionsettings.usecases.GetDelayDialogUseCase
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.TemporaryShortcutRepository
import ch.rmy.android.http_shortcuts.data.enums.ShortcutExecutionType
import ch.rmy.android.http_shortcuts.data.models.ShortcutModel
import ch.rmy.android.http_shortcuts.extensions.type
import ch.rmy.android.http_shortcuts.tiles.QuickSettingsTileManager
import ch.rmy.android.http_shortcuts.utils.LauncherShortcutManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class ExecutionSettingsViewModel(application: Application) : BaseViewModel<Unit, ExecutionSettingsViewState>(application), WithDialog {

    @Inject
    lateinit var temporaryShortcutRepository: TemporaryShortcutRepository

    @Inject
    lateinit var getDelayDialog: GetDelayDialogUseCase

    @Inject
    lateinit var launcherShortcutManager: LauncherShortcutManager

    init {
        getApplicationComponent().inject(this)
    }

    override var dialogState: DialogState?
        get() = currentViewState?.dialogState
        set(value) {
            updateViewState {
                copy(dialogState = value)
            }
        }

    override fun onInitializationStarted(data: Unit) {
        finalizeInitialization(silent = true)
    }

    override fun initViewState() = ExecutionSettingsViewState(
        launcherShortcutOptionVisible = launcherShortcutManager.supportsLauncherShortcuts(),
        quickSettingsTileShortcutOptionVisible = QuickSettingsTileManager.supportsQuickSettingsTiles(),
    )

    override fun onInitialized() {
        viewModelScope.launch {
            try {
                val temporaryShortcut = temporaryShortcutRepository.getTemporaryShortcut()
                initViewStateFromShortcut(temporaryShortcut)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                onInitializationError(e)
            }
        }
    }

    private fun initViewStateFromShortcut(shortcut: ShortcutModel) {
        updateViewState {
            copy(
                waitForConnection = shortcut.isWaitForNetwork,
                waitForConnectionOptionVisible = shortcut.type == ShortcutExecutionType.APP,
                launcherShortcut = shortcut.launcherShortcut,
                quickSettingsTileShortcut = shortcut.quickSettingsTileShortcut,
                delay = shortcut.delay.milliseconds,
                requireConfirmation = shortcut.requireConfirmation,
                excludeFromHistory = shortcut.excludeFromHistory,
            )
        }
    }

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

    fun onQuickSettingsTileShortcutChanged(quickSettingsTileShortcut: Boolean) {
        updateViewState {
            copy(quickSettingsTileShortcut = quickSettingsTileShortcut)
        }
        launchWithProgressTracking {
            temporaryShortcutRepository.setQuickSettingsTileShortcut(quickSettingsTileShortcut)
        }
    }

    private fun onDelayChanged(delay: Duration) {
        updateViewState {
            copy(delay = delay)
        }
        launchWithProgressTracking {
            temporaryShortcutRepository.setDelay(delay)
        }
    }

    fun onRequireConfirmationChanged(requireConfirmation: Boolean) {
        updateViewState {
            copy(requireConfirmation = requireConfirmation)
        }
        launchWithProgressTracking {
            temporaryShortcutRepository.setRequireConfirmation(requireConfirmation)
        }
    }

    fun onDelayButtonClicked() {
        showDelayDialog()
    }

    private fun showDelayDialog() {
        doWithViewState { viewState ->
            dialogState = getDelayDialog(
                delay = viewState.delay,
                getLabel = ::DurationLocalizable,
                onDelayChanged = ::onDelayChanged,
            )
        }
    }

    fun onBackPressed() {
        viewModelScope.launch {
            waitForOperationsToFinish()
            finish()
        }
    }
}

package ch.rmy.android.http_shortcuts.activities.editor.executionsettings

import android.app.Application
import ch.rmy.android.framework.extensions.attachTo
import ch.rmy.android.framework.utils.localization.DurationLocalizable
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.TemporaryShortcutRepository
import ch.rmy.android.http_shortcuts.data.models.ShortcutModel
import ch.rmy.android.http_shortcuts.tiles.QuickSettingsTileManager
import ch.rmy.android.http_shortcuts.utils.LauncherShortcutManager
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class ExecutionSettingsViewModel(application: Application) : BaseViewModel<Unit, ExecutionSettingsViewState>(application) {

    private val temporaryShortcutRepository = TemporaryShortcutRepository()

    override fun onInitializationStarted(data: Unit) {
        finalizeInitialization(silent = true)
    }

    override fun initViewState() = ExecutionSettingsViewState(
        launcherShortcutOptionVisible = LauncherShortcutManager.supportsLauncherShortcuts(),
        quickSettingsTileShortcutOptionVisible = QuickSettingsTileManager.supportsQuickSettingsTiles(),
    )

    override fun onInitialized() {
        temporaryShortcutRepository.getObservableTemporaryShortcut()
            .subscribe(
                ::initViewStateFromShortcut,
                ::onInitializationError,
            )
            .attachTo(destroyer)
    }

    private fun initViewStateFromShortcut(shortcut: ShortcutModel) {
        updateViewState {
            copy(
                waitForConnection = shortcut.isWaitForNetwork,
                launcherShortcut = shortcut.launcherShortcut,
                quickSettingsTileShortcut = shortcut.quickSettingsTileShortcut,
                delay = shortcut.delay.milliseconds,
                requireConfirmation = shortcut.requireConfirmation,
            )
        }
    }

    private fun onInitializationError(error: Throwable) {
        handleUnexpectedError(error)
        finish()
    }

    fun onWaitForConnectionChanged(waitForConnection: Boolean) {
        performOperation(
            temporaryShortcutRepository.setWaitForConnection(waitForConnection)
        )
    }

    fun onLauncherShortcutChanged(launcherShortcut: Boolean) {
        performOperation(
            temporaryShortcutRepository.setLauncherShortcut(launcherShortcut)
        )
    }

    fun onQuickSettingsTileShortcutChanged(quickSettingsTileShortcut: Boolean) {
        performOperation(
            temporaryShortcutRepository.setQuickSettingsTileShortcut(quickSettingsTileShortcut)
        )
    }

    fun onDelayChanged(delay: Duration) {
        performOperation(
            temporaryShortcutRepository.setDelay(delay)
        )
    }

    fun onRequireConfirmationChanged(requireConfirmation: Boolean) {
        performOperation(
            temporaryShortcutRepository.setRequireConfirmation(requireConfirmation)
        )
    }

    fun onDelayButtonClicked() {
        emitEvent(
            ExecutionSettingsEvent.ShowDelayDialog(currentViewState.delay) { duration ->
                DurationLocalizable(duration)
            }
        )
    }

    fun onBackPressed() {
        waitForOperationsToFinish {
            finish()
        }
    }
}

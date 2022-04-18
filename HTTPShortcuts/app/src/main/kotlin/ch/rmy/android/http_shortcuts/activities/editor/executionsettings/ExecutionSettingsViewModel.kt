package ch.rmy.android.http_shortcuts.activities.editor.executionsettings

import android.app.Application
import ch.rmy.android.framework.extensions.attachTo
import ch.rmy.android.framework.extensions.context
import ch.rmy.android.framework.utils.localization.DurationLocalizable
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.framework.viewmodel.WithDialog
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.activities.editor.executionsettings.usecases.GetDelayDialogUseCase
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.TemporaryShortcutRepository
import ch.rmy.android.http_shortcuts.data.models.ShortcutModel
import ch.rmy.android.http_shortcuts.tiles.QuickSettingsTileManager
import ch.rmy.android.http_shortcuts.utils.LauncherShortcutManager
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class ExecutionSettingsViewModel(application: Application) : BaseViewModel<Unit, ExecutionSettingsViewState>(application), WithDialog {

    private val temporaryShortcutRepository = TemporaryShortcutRepository()
    private val getDelayDialog = GetDelayDialogUseCase()
    private val launcherShortcutManager = LauncherShortcutManager(context)

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
        showDelayDialog()
    }

    private fun showDelayDialog() {
        doWithViewState { viewState ->
            dialogState = getDelayDialog(
                delay = viewState.delay,
                getLabel = { duration ->
                    DurationLocalizable(duration)
                },
                onDelayChanged = ::onDelayChanged,
            )
        }
    }

    fun onBackPressed() {
        waitForOperationsToFinish {
            finish()
        }
    }
}

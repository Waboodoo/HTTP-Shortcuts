package ch.rmy.android.http_shortcuts.activities.misc.second_launcher

import android.app.Application
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.activities.ExecuteActivity
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.enums.ShortcutTriggerType
import ch.rmy.android.http_shortcuts.extensions.toShortcutPlaceholder
import javax.inject.Inject

class SecondLauncherViewModel(application: Application) : BaseViewModel<Unit, SecondLauncherViewState>(application) {

    @Inject
    lateinit var shortcutRepository: ShortcutRepository

    init {
        getApplicationComponent().inject(this)
    }

    override suspend fun initialize(data: Unit): SecondLauncherViewState {
        val shortcuts = shortcutRepository.getShortcuts()
            .filter {
                it.secondaryLauncherShortcut
            }
        shortcuts.singleOrNull()
            ?.let {
                executeShortcut(it.id)
                terminateInitialization()
            }
            ?: return SecondLauncherViewState(
                dialogState = SecondLauncherDialogState.PickShortcut(
                    shortcuts.map { it.toShortcutPlaceholder() },
                ),
            )
    }

    private suspend fun executeShortcut(shortcutId: ShortcutId) {
        openActivity(ExecuteActivity.IntentBuilder(shortcutId).trigger(ShortcutTriggerType.SECONDARY_LAUNCHER_APP))
    }

    fun onShortcutSelected(shortcutId: ShortcutId) = runAction {
        executeShortcut(shortcutId)
        finish(skipAnimation = true)
    }

    fun onDialogDismissed() = runAction {
        finish(skipAnimation = true)
    }
}

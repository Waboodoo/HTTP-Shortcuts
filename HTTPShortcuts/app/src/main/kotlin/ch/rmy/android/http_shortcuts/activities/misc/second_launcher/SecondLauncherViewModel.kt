package ch.rmy.android.http_shortcuts.activities.misc.second_launcher

import android.app.Application
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.activities.execute.ExecutionStarter
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.enums.ShortcutTriggerType
import ch.rmy.android.http_shortcuts.extensions.toShortcutPlaceholder
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SecondLauncherViewModel
@Inject
constructor(
    application: Application,
    private val shortcutRepository: ShortcutRepository,
    private val executionStarter: ExecutionStarter,
) : BaseViewModel<Unit, SecondLauncherViewState>(application) {

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

    private fun executeShortcut(shortcutId: ShortcutId) {
        executionStarter.execute(
            shortcutId = shortcutId,
            trigger = ShortcutTriggerType.SECONDARY_LAUNCHER_APP,
        )
    }

    fun onShortcutSelected(shortcutId: ShortcutId) = runAction {
        executeShortcut(shortcutId)
        finish(skipAnimation = true)
    }

    fun onDialogDismissed() = runAction {
        finish(skipAnimation = true)
    }
}

package ch.rmy.android.http_shortcuts.activities.misc.quick_settings_tile

import android.app.Application
import ch.rmy.android.framework.viewmodel.BaseViewModel
import ch.rmy.android.http_shortcuts.activities.execute.ExecutionStarter
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.enums.ShortcutTriggerType
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.extensions.toShortcutPlaceholder
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class QuickSettingsTileViewModel
@Inject
constructor(
    application: Application,
    private val shortcutRepository: ShortcutRepository,
    private val executionStarter: ExecutionStarter,
) : BaseViewModel<Unit, QuickSettingsTileViewState>(application) {

    private lateinit var shortcuts: List<Shortcut>

    override suspend fun initialize(data: Unit): QuickSettingsTileViewState {
        shortcuts = shortcutRepository.getShortcuts()
            .filter {
                it.quickSettingsTileShortcut
            }

        return when (shortcuts.size) {
            0 -> QuickSettingsTileViewState(
                dialogState = QuickSettingsTileDialogState.Instructions,
            )
            1 -> {
                executeShortcut(shortcuts.first().id)
                terminateInitialization()
            }
            else -> QuickSettingsTileViewState(
                dialogState = QuickSettingsTileDialogState.PickShortcut(
                    shortcuts.map { it.toShortcutPlaceholder() },
                ),
            )
        }
    }

    private fun executeShortcut(shortcutId: ShortcutId) {
        executionStarter.execute(
            shortcutId = shortcutId,
            trigger = ShortcutTriggerType.QUICK_SETTINGS_TILE,
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

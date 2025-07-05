package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.data.domains.pending_executions.PendingExecutionsRepository
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutNameOrId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.scheduling.ExecutionScheduler
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import javax.inject.Inject

class CancelShortcutAction
@Inject
constructor(
    private val shortcutRepository: ShortcutRepository,
    private val pendingExecutionsRepository: PendingExecutionsRepository,
    private val executionScheduler: ExecutionScheduler,
) : Action<CancelShortcutAction.Params> {
    override suspend fun Params.execute(executionContext: ExecutionContext) {
        val shortcut = try {
            shortcutRepository.getShortcutByNameOrId(shortcutNameOrId ?: executionContext.shortcutId)
        } catch (_: NoSuchElementException) {
            return
        }

        pendingExecutionsRepository.removePendingExecutionsForShortcut(shortcut.id)
        executionScheduler.schedule()
    }

    data class Params(
        val shortcutNameOrId: ShortcutNameOrId?,
    )
}

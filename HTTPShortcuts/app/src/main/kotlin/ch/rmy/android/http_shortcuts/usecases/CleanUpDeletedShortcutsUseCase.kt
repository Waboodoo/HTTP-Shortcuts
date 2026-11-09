package ch.rmy.android.http_shortcuts.usecases

import ch.rmy.android.http_shortcuts.data.domains.pending_executions.PendingExecutionsRepository
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.widgets.ShortcutWidgetsRepository
import ch.rmy.android.http_shortcuts.data.enums.PendingExecutionType
import ch.rmy.android.http_shortcuts.scheduling.AlarmScheduler
import ch.rmy.android.http_shortcuts.utils.LauncherShortcutManager
import javax.inject.Inject

class CleanUpDeletedShortcutsUseCase
@Inject
constructor(
    private val pendingExecutionsRepository: PendingExecutionsRepository,
    private val shortcutWidgetsRepository: ShortcutWidgetsRepository,
    private val launcherShortcutManager: LauncherShortcutManager,
    private val alarmScheduler: AlarmScheduler,
) {
    suspend operator fun invoke(shortcutIds: Set<ShortcutId>) {
        cancelAlarms(shortcutIds)
        pendingExecutionsRepository.removePendingExecutionsForShortcuts(shortcutIds)
        shortcutWidgetsRepository.deleteDeadShortcutWidgets()
        launcherShortcutManager.removeShortcuts(shortcutIds)
    }

    private suspend fun cancelAlarms(shortcutIds: Set<ShortcutId>) {
        shortcutIds.flatMap { shortcutId ->
            pendingExecutionsRepository.getPendingExecutionsForShortcut(shortcutId)
        }
            .filter { it.type == PendingExecutionType.REPEAT }
            .forEach { pendingExecution ->
                alarmScheduler.cancelAlarm(pendingExecution.id, pendingExecution.requestCode)
            }
    }
}

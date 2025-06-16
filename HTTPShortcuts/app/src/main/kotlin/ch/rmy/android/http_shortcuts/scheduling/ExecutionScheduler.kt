package ch.rmy.android.http_shortcuts.scheduling

import android.content.Context
import ch.rmy.android.framework.extensions.minus
import ch.rmy.android.framework.extensions.tryOrLog
import ch.rmy.android.http_shortcuts.data.domains.pending_executions.PendingExecutionsRepository
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.enums.PendingExecutionType
import java.time.Instant
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class ExecutionScheduler
@Inject
constructor(
    private val context: Context,
    private val pendingExecutionsRepository: PendingExecutionsRepository,
    private val shortcutRepository: ShortcutRepository,
    private val executionStarter: ExecutionWorker.Starter,
    private val alarmScheduler: AlarmScheduler,
) {

    suspend fun schedule() {
        schedule(withNetworkConstraints = true)
        schedule(withNetworkConstraints = false)
    }

    private suspend fun schedule(withNetworkConstraints: Boolean) {
        val nextPendingExecution = pendingExecutionsRepository.getNextPendingExecution(withNetworkConstraints)
            ?: return

        val delayUntil = nextPendingExecution.delayUntil
        if (delayUntil == null && !withNetworkConstraints) {
            ExecutionWorker.runPendingExecution(context, nextPendingExecution)
        } else {
            tryOrLog {
                if (delayUntil != null && nextPendingExecution.type == PendingExecutionType.REPEAT && !withNetworkConstraints) {
                    alarmScheduler.createAlarm(
                        nextPendingExecution.id,
                        nextPendingExecution.requestCode,
                        delayUntil,
                        interval = getInterval(nextPendingExecution.shortcutId),
                    )
                } else {
                    val delay = delayUntil?.let { it - Instant.now() }?.takeUnless { it.isNegative() }
                    executionStarter(nextPendingExecution.id, delay, withNetworkConstraints)
                }
            }
        }
    }

    private suspend fun getInterval(shortcutId: ShortcutId): Duration =
        try {
            shortcutRepository.getShortcutById(shortcutId).repetitionInterval?.minutes
        } catch (_: NoSuchElementException) {
            null
        }
            // if the shortcut does not exist or is not repeating, then we use an arbitrary interval
            // here, to ensure that the execution is still started, which will in turn cancel the schedule
            ?: 10.minutes
}

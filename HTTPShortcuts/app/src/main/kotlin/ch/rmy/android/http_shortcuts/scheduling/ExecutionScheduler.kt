package ch.rmy.android.http_shortcuts.scheduling

import android.content.Context
import ch.rmy.android.framework.extensions.minus
import ch.rmy.android.framework.extensions.tryOrLog
import ch.rmy.android.http_shortcuts.data.domains.pending_executions.PendingExecutionsRepository
import ch.rmy.android.http_shortcuts.data.enums.PendingExecutionType
import java.time.Instant
import javax.inject.Inject
import kotlin.time.Duration

class ExecutionScheduler
@Inject
constructor(
    private val context: Context,
    private val pendingExecutionsRepository: PendingExecutionsRepository,
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

        val delay = calculateDelay(nextPendingExecution.delayUntil)
        if (delay == null && !withNetworkConstraints) {
            ExecutionWorker.runPendingExecution(context, nextPendingExecution)
        } else {
            tryOrLog {
                if (delay != null && nextPendingExecution.type == PendingExecutionType.REPEAT && !withNetworkConstraints) {
                    alarmScheduler.createAlarm(nextPendingExecution.id, nextPendingExecution.requestCode, delay)
                } else {
                    executionStarter(nextPendingExecution.id, delay, withNetworkConstraints)
                }
            }
        }
    }

    private fun calculateDelay(waitUntil: Instant?): Duration? {
        if (waitUntil == null) {
            return null
        }
        val now = Instant.now()
        if (waitUntil < now) {
            return null
        }
        return waitUntil - now
    }
}

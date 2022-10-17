package ch.rmy.android.http_shortcuts.scheduling

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import ch.rmy.android.framework.extensions.runIf
import ch.rmy.android.framework.extensions.runIfNotNull
import ch.rmy.android.framework.extensions.tryOrLog
import ch.rmy.android.http_shortcuts.data.domains.pending_executions.PendingExecutionsRepository
import ch.rmy.android.http_shortcuts.data.models.PendingExecutionModel
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ExecutionScheduler
@Inject
constructor(
    private val context: Context,
    private val pendingExecutionsRepository: PendingExecutionsRepository,
) {

    suspend fun schedule() {
        schedule(withNetworkConstraints = true)
        schedule(withNetworkConstraints = false)
    }

    private suspend fun schedule(withNetworkConstraints: Boolean) {
        val nextPendingExecution = pendingExecutionsRepository.getNextPendingExecution(withNetworkConstraints)
            ?: return

        val delay = calculateDelay(nextPendingExecution.waitUntil)
        if (delay == null && !withNetworkConstraints) {
            ExecutionWorker.runPendingExecution(context, nextPendingExecution)
        } else {
            tryOrLog {
                scheduleService(nextPendingExecution, delay, withNetworkConstraints)
            }
        }
    }

    private fun scheduleService(pendingExecution: PendingExecutionModel, delay: Long?, withNetworkConstraints: Boolean) {
        with(WorkManager.getInstance(context)) {
            cancelAllWorkByTag(TAG)
            enqueue(
                OneTimeWorkRequestBuilder<ExecutionWorker>()
                    .addTag(TAG)
                    .setInputData(
                        Data.Builder()
                            .putString(ExecutionWorker.INPUT_EXECUTION_ID, pendingExecution.id)
                            .build()
                    )
                    .runIfNotNull(delay) {
                        setInitialDelay(it, TimeUnit.MILLISECONDS)
                    }
                    .runIf(withNetworkConstraints) {
                        setConstraints(
                            Constraints.Builder()
                                .setRequiredNetworkType(NetworkType.CONNECTED)
                                .build()
                        )
                    }
                    .build()
            )
        }
    }

    private fun calculateDelay(waitUntil: Date?): Long? {
        if (waitUntil == null) {
            return null
        }
        val now = Calendar.getInstance().time
        val difference = waitUntil.time - now.time
        return difference.takeIf { it > 0L }
    }

    companion object {
        private const val TAG = "execution_scheduler"
    }
}

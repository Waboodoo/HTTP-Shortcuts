package ch.rmy.android.http_shortcuts.scheduling

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.extensions.mapIf
import ch.rmy.android.framework.extensions.mapIfNotNull
import ch.rmy.android.http_shortcuts.data.domains.pending_executions.PendingExecutionsRepository
import ch.rmy.android.http_shortcuts.data.models.PendingExecution
import io.reactivex.Completable
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

class ExecutionScheduler(private val context: Context) {

    private val pendingExecutionsRepository = PendingExecutionsRepository()

    fun schedule(): Completable =
        schedule(withNetworkConstraints = true)
            .mergeWith(schedule(withNetworkConstraints = false))

    private fun schedule(withNetworkConstraints: Boolean): Completable =
        pendingExecutionsRepository.getNextPendingExecution(withNetworkConstraints)
            .flatMapCompletable { nextPendingExecutionOptional ->
                Completable.fromAction {
                    val nextPendingExecution = nextPendingExecutionOptional.value ?: return@fromAction
                    val delay = calculateDelay(nextPendingExecution.waitUntil)

                    if (delay == null && !withNetworkConstraints) {
                        ExecutionWorker.runPendingExecution(context, nextPendingExecution)
                    } else {
                        try {
                            scheduleService(nextPendingExecution, delay, withNetworkConstraints)
                        } catch (e: Exception) {
                            logException(e)
                        }
                    }
                }
            }

    private fun scheduleService(pendingExecution: PendingExecution, delay: Long?, withNetworkConstraints: Boolean) {
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
                    .mapIfNotNull(delay) {
                        setInitialDelay(it, TimeUnit.MILLISECONDS)
                    }
                    .mapIf(withNetworkConstraints) {
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

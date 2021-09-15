package ch.rmy.android.http_shortcuts.scheduling

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import ch.rmy.android.http_shortcuts.data.DataSource
import ch.rmy.android.http_shortcuts.data.models.PendingExecution
import ch.rmy.android.http_shortcuts.extensions.logException
import ch.rmy.android.http_shortcuts.extensions.mapIf
import ch.rmy.android.http_shortcuts.extensions.mapIfNotNull
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

object ExecutionScheduler {

    private const val TAG = "execution_scheduler"

    fun schedule(context: Context) {
        schedule(context, withNetworkConstraints = true)
        schedule(context, withNetworkConstraints = false)
    }

    private fun schedule(context: Context, withNetworkConstraints: Boolean) {
        val nextPendingExecution = DataSource.getNextPendingExecution(withNetworkConstraints) ?: return
        val delay = calculateDelay(nextPendingExecution.waitUntil)

        if (delay == null && !withNetworkConstraints) {
            ExecutionWorker.runPendingExecution(context, nextPendingExecution)
        } else {
            try {
                scheduleService(context, nextPendingExecution, delay, withNetworkConstraints)
            } catch (e: Exception) {
                logException(e)
            }
        }
    }

    private fun scheduleService(context: Context, pendingExecution: PendingExecution, delay: Long?, withNetworkConstraints: Boolean) {
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

}

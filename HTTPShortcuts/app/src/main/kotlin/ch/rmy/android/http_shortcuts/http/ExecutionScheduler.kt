package ch.rmy.android.http_shortcuts.http

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.os.PersistableBundle
import androidx.annotation.RequiresApi
import ch.rmy.android.http_shortcuts.activities.ExecuteActivity
import ch.rmy.android.http_shortcuts.data.Commons
import ch.rmy.android.http_shortcuts.data.Controller
import ch.rmy.android.http_shortcuts.data.models.PendingExecution
import ch.rmy.android.http_shortcuts.extensions.detachFromRealm
import ch.rmy.android.http_shortcuts.extensions.logException
import ch.rmy.android.http_shortcuts.extensions.mapIf
import ch.rmy.android.http_shortcuts.utils.UUIDUtils
import java.util.Calendar
import java.util.Date

object ExecutionScheduler {

    fun schedule(context: Context) {
        val nextPendingExecution = getNextPendingExecution() ?: return

        if (shouldExecuteNow(nextPendingExecution)) {
            processPendingExecution(context, nextPendingExecution)
        } else {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    scheduleService(context, nextPendingExecution)
                }
            } catch (e: Exception) {
                logException(e)
            }
        }
    }

    private fun getNextPendingExecution(): PendingExecution? =
        Controller().use { controller ->
            controller.getPendingExecutions()
                .minByOrNull { it.waitUntil ?: Date(0) }
                ?.detachFromRealm()
        }

    private fun shouldExecuteNow(pendingExecution: PendingExecution): Boolean =
        pendingExecution.waitUntil
            ?.let { it < Calendar.getInstance().time }
            ?: true

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun scheduleService(context: Context, pendingExecution: PendingExecution) {
        val delay = calculateDelay(pendingExecution.waitUntil)
        val jobId = UUIDUtils.toLong(pendingExecution.shortcutId).toInt()
        val jobInfo = JobInfo.Builder(jobId, ComponentName(context, ExecutionService::class.java))
            .setExtras(PersistableBundle().apply { putString(ExecutionService.PARAM_EXECUTION_ID, pendingExecution.id) })
            .mapIf(delay != null) {
                it.setMinimumLatency(delay!!)
            }
            .mapIf(pendingExecution.waitForNetwork) {
                it.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
            }
            .build()
        (context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler).schedule(jobInfo)
    }

    private fun calculateDelay(waitUntil: Date?): Long? {
        if (waitUntil == null) {
            return null
        }
        val now = Calendar.getInstance().time
        val difference = waitUntil.time - now.time
        return difference.takeIf { it > 0L }
    }

    fun processPendingExecution(context: Context, pendingExecution: PendingExecution, callback: () -> Unit = {}) {
        val shortcutId = pendingExecution.shortcutId
        val tryNumber = pendingExecution.tryNumber
        val variableValues = pendingExecution.resolvedVariables
            .associate { variable -> variable.key to variable.value }
        val recursionDepth = pendingExecution.recursionDepth
        Commons.removePendingExecution(pendingExecution.id)
            .doOnTerminate {
                executeShortcut(context, shortcutId, variableValues, tryNumber, recursionDepth)
                callback()
            }
            .subscribe()
    }

    private fun executeShortcut(context: Context, id: String, variableValues: Map<String, String>, tryNumber: Int, recursionDepth: Int) {
        ExecuteActivity.IntentBuilder(context, id)
            .variableValues(variableValues)
            .tryNumber(tryNumber)
            .recursionDepth(recursionDepth)
            .startActivity(context)
    }
}

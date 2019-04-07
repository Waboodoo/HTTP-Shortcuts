package ch.rmy.android.http_shortcuts.http

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.os.PersistableBundle
import androidx.annotation.RequiresApi
import ch.rmy.android.http_shortcuts.data.Controller
import ch.rmy.android.http_shortcuts.extensions.logException
import ch.rmy.android.http_shortcuts.extensions.mapIf
import ch.rmy.android.http_shortcuts.utils.UUIDUtils
import java.util.*

object ExecutionScheduler {

    fun schedule(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                scheduleService(context)
            }
        } catch (e: Exception) {
            logException(e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun scheduleService(context: Context) {
        Controller().use { controller ->
            val pendingExecutions = controller.getShortcutsPendingExecution()
            pendingExecutions.forEach { pendingExecution ->
                val delay = calculateDelay(pendingExecution.waitUntil)
                val jobId = UUIDUtils.toLong(pendingExecution.shortcutId).toInt()
                val jobInfo = JobInfo.Builder(jobId, ComponentName(context, ExecutionService::class.java))
                    .setExtras(PersistableBundle().apply { putString(ExecutionService.PARAM_SHORTCUT_ID, pendingExecution.shortcutId) })
                    .setMinimumLatency(delay)
                    .mapIf(pendingExecution.waitForNetwork) {
                        it.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    }
                    .build()
                (context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler).schedule(jobInfo)
            }
        }
    }

    private fun calculateDelay(waitUntil: Date?): Long {
        if (waitUntil == null) {
            return MIN_DELAY
        }
        val now = Calendar.getInstance().time
        val difference = waitUntil.time - now.time
        return maxOf(difference, MIN_DELAY)
    }

    private const val MIN_DELAY = 300L

}
package ch.rmy.android.http_shortcuts.http

import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.support.annotation.RequiresApi
import ch.rmy.android.http_shortcuts.activities.ExecuteActivity
import ch.rmy.android.http_shortcuts.realm.Controller
import ch.rmy.android.http_shortcuts.utils.Destroyer
import ch.rmy.android.http_shortcuts.utils.logException
import ch.rmy.android.http_shortcuts.utils.mapIf
import java.util.*

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class ExecutionService : JobService() {

    val context: Context
        get() = this

    private val destroyer = Destroyer()

    val controller: Controller by lazy {
        destroyer.own(Controller())
    }

    override fun onStartJob(params: JobParameters): Boolean {
        val shortcutId = params.jobId.toLong()
        val pendingExecution = controller.getShortcutPendingExecution(shortcutId) ?: return false
        val tryNumber = pendingExecution.tryNumber + 1
        val variableValues = pendingExecution.resolvedVariables
                .associate { variable -> variable.key to variable.value }
        controller.removePendingExecution(shortcutId)
                .always { _, _, _ ->
                    executeShortcut(shortcutId, variableValues, tryNumber)
                    jobFinished(params, false)
                }
        return true
    }

    override fun onStopJob(params: JobParameters) = false

    private fun executeShortcut(id: Long, variableValues: Map<String, String>, tryNumber: Int) {
        val shortcutIntent = ExecuteActivity.IntentBuilder(context, id)
                .variableValues(variableValues)
                .tryNumber(tryNumber)
                .build()
        startActivity(shortcutIntent)
    }

    override fun onCreate() {
        super.onCreate()
        Controller.init(applicationContext)
    }

    override fun onDestroy() {
        destroyer.destroy()
    }

    companion object {

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
                    val jobInfo = JobInfo.Builder(pendingExecution.shortcutId.toInt(), ComponentName(context, ExecutionService::class.java))
                            .mapIf(pendingExecution.waitUntil != null) {
                                val now = Calendar.getInstance().time
                                val then = pendingExecution.waitUntil!!
                                val latency = then.time - now.time
                                it.mapIf(latency > 0) {
                                    it.setMinimumLatency(latency)
                                }
                            }
                            .mapIf(pendingExecution.waitForNetwork) {
                                it.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                            }
                            .build()
                    (context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler).schedule(jobInfo)
                }
            }
        }

    }

}

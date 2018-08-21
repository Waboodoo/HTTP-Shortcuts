package ch.rmy.android.http_shortcuts.http

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.os.Build
import android.support.annotation.RequiresApi
import ch.rmy.android.http_shortcuts.activities.ExecuteActivity
import ch.rmy.android.http_shortcuts.realm.Controller
import ch.rmy.android.http_shortcuts.utils.Destroyer

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
                .doOnTerminate {
                    executeShortcut(shortcutId, variableValues, tryNumber)
                    jobFinished(params, false)
                }
                .subscribe()
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

}

package ch.rmy.android.http_shortcuts.http

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import ch.rmy.android.http_shortcuts.activities.ExecuteActivity
import ch.rmy.android.http_shortcuts.data.Commons
import ch.rmy.android.http_shortcuts.data.Controller
import ch.rmy.android.http_shortcuts.data.RealmFactory
import ch.rmy.android.http_shortcuts.extensions.startActivity
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
        val shortcutId = params.extras.getString(PARAM_SHORTCUT_ID) ?: ""
        val pendingExecution = controller.getShortcutPendingExecution(shortcutId) ?: return false
        val tryNumber = pendingExecution.tryNumber + 1
        val variableValues = pendingExecution.resolvedVariables
            .associate { variable -> variable.key to variable.value }
        val recursionDepth = pendingExecution.recursionDepth
        Commons.removePendingExecution(shortcutId)
            .doOnTerminate {
                executeShortcut(shortcutId, variableValues, tryNumber, recursionDepth)
                jobFinished(params, false)
            }
            .subscribe()
        return true
    }

    override fun onStopJob(params: JobParameters) = false

    private fun executeShortcut(id: String, variableValues: Map<String, String>, tryNumber: Int, recursionDepth: Int) {
        ExecuteActivity.IntentBuilder(context, id)
            .variableValues(variableValues)
            .tryNumber(tryNumber)
            .recursionDepth(recursionDepth)
            .build()
            .startActivity(context)
    }

    override fun onCreate() {
        super.onCreate()
        RealmFactory.init(applicationContext)
    }

    override fun onDestroy() {
        destroyer.destroy()
    }

    companion object {

        const val PARAM_SHORTCUT_ID = "shortcutId"

    }

}

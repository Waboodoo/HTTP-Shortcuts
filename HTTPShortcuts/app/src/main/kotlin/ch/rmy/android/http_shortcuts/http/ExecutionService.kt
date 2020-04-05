package ch.rmy.android.http_shortcuts.http

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import ch.rmy.android.http_shortcuts.data.Controller
import ch.rmy.android.http_shortcuts.data.RealmFactory
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
        val shortcutId = params.extras.getString(PARAM_SHORTCUT_ID) ?: return false
        val pendingExecution = controller.getShortcutPendingExecution(shortcutId) ?: return false
        ExecutionScheduler.processPendingExecution(context, pendingExecution) {
            jobFinished(params, false)
        }
        return true
    }

    override fun onStopJob(params: JobParameters) = false


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

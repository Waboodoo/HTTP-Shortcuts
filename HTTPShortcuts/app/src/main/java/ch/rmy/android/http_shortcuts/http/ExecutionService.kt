package ch.rmy.android.http_shortcuts.http

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.os.Build
import android.os.IBinder
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.ExecuteActivity
import ch.rmy.android.http_shortcuts.activities.MainActivity
import ch.rmy.android.http_shortcuts.realm.Controller
import ch.rmy.android.http_shortcuts.realm.models.PendingExecution
import ch.rmy.android.http_shortcuts.utils.Connectivity
import ch.rmy.android.http_shortcuts.utils.CrashReporting
import ch.rmy.android.http_shortcuts.utils.NotificationUtil
import ch.rmy.android.http_shortcuts.utils.mapIf
import io.realm.RealmResults
import java.util.*

class ExecutionService : Service() {

    val context = this

    val controller: Controller by lazy {
        Controller()
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (Connectivity.isNetworkConnected(context)) {
                start(context)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val pendingExecutions = controller.shortcutsPendingExecution
        if (pendingExecutions.isEmpty()) {
            stopSelf()
        } else {
            updateNotification(pendingExecutions.size)

            while (Connectivity.isNetworkConnected(context)) {
                val pendingExecution = getNextToProcess(pendingExecutions) ?: break

                val id = pendingExecution.shortcutId
                val tryNumber = pendingExecution.tryNumber + 1
                val variableValues = pendingExecution.resolvedVariables
                        .associate { variable ->
                            variable.key to variable.value
                        }

                controller.removePendingExecutionSynchronously(pendingExecution)

                try {
                    Thread.sleep(INITIAL_DELAY.toLong())
                    executeShortcut(id, variableValues, tryNumber)
                } catch (e: InterruptedException) {
                    break
                }

                if (pendingExecutions.isEmpty()) {
                    stopSelf()
                    break
                }
                updateNotification(pendingExecutions.size)
            }
        }

        return Service.START_STICKY
    }

    private fun getNextToProcess(pendingExecutions: RealmResults<PendingExecution>): PendingExecution? {
        val now = Calendar.getInstance().time
        return pendingExecutions.firstOrNull {
            it.waitUntil?.before(now) == true
        }
    }

    private fun updateNotification(size: Int) {
        val intent = Intent(context, MainActivity::class.java)
        val notification = NotificationCompat.Builder(context, NotificationUtil.PENDING_SHORTCUTS_NOTIFICATION_CHANNEL)
                .setContentTitle(getString(R.string.title_shortcuts_pending))
                .setContentText(context.resources.getQuantityString(R.plurals.message_shortcuts_pending, size, size))
                .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.ic_launcher))
                .setContentIntent(PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT))
                .setSmallIcon(R.drawable.ic_waiting_white)
                .build()
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun executeShortcut(id: Long, variableValues: Map<String, String>, tryNumber: Int) {
        val shortcutIntent = ExecuteActivity.IntentBuilder(context, id)
                .variableValues(variableValues)
                .tryNumber(tryNumber)
                .build()
        startActivity(shortcutIntent)
    }

    override fun onCreate() {
        super.onCreate()
        registerReceiver(receiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
    }

    override fun onDestroy() {
        unregisterReceiver(receiver)
        controller.destroy()
    }

    companion object {

        private const val INITIAL_DELAY = 1500

        private const val NOTIFICATION_ID = 1

        private const val JOB_ID = 1

        fun start(context: Context, executionTime: Date? = null) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    scheduleService(context, executionTime)
                } else {
                    if (executionTime != null) {
                        val now = Calendar.getInstance().time
                        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                        val serviceIntent = Intent(context, RestarterService::class.java)
                        val pendingIntent = PendingIntent.getBroadcast(context, 1, serviceIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + executionTime.time - now.time, pendingIntent)
                    } else {
                        context.startService(Intent(context, ExecutionService::class.java))
                    }
                }
            } catch (e: Exception) {
                CrashReporting.logException(e)
            }
        }

        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        private fun scheduleService(context: Context, executionTime: Date? = null) {
            val jobInfo = JobInfo.Builder(JOB_ID, ComponentName(context, ExecutionService::class.java))
                    .mapIf(executionTime != null) {
                        val now = Calendar.getInstance().time
                        val latency = now.time - executionTime!!.time
                        it.setMinimumLatency(latency)
                    }
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .build()
            (context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler).schedule(jobInfo)
        }

    }

}

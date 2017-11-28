package ch.rmy.android.http_shortcuts.http

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.MainActivity
import ch.rmy.android.http_shortcuts.realm.Controller
import ch.rmy.android.http_shortcuts.realm.models.PendingExecution
import ch.rmy.android.http_shortcuts.utils.Connectivity
import ch.rmy.android.http_shortcuts.utils.IntentUtil
import ch.rmy.android.http_shortcuts.utils.NotificationUtil
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
                val variableValues = HashMap<String, String>()
                for (resolvedVariable in pendingExecution.resolvedVariables!!) {
                    variableValues.put(resolvedVariable.key!!, resolvedVariable.value!!)
                }

                controller.removePendingExecution(pendingExecution)

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
        for (pendingExecution in pendingExecutions) {
            val waitUntil = pendingExecution.waitUntil
            if (waitUntil == null || waitUntil.before(now)) {
                return pendingExecution
            }
        }
        return null
    }

    private fun updateNotification(size: Int) {
        val intent = Intent(context, MainActivity::class.java)
        val builder = NotificationCompat.Builder(context, NotificationUtil.PENDING_SHORTCUTS_NOTIFICATION_CHANNEL)
                .setContentTitle(getString(R.string.title_shortcuts_pending))
                .setContentText(context.resources.getQuantityString(R.plurals.message_shortcuts_pending, size, size))
                .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.ic_launcher))
                .setContentIntent(PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT))
                .setSmallIcon(R.drawable.ic_waiting_white)
        startForeground(NOTIFICATION_ID, builder.build())
    }

    private fun executeShortcut(id: Long, variableValues: HashMap<String, String>, tryNumber: Int) {
        val shortcutIntent = IntentUtil.createIntent(context, id, variableValues, tryNumber)
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

        fun start(context: Context, executionTime: Date? = null) {
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

    }

}

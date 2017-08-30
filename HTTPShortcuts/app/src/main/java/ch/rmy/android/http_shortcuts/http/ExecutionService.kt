package ch.rmy.android.http_shortcuts.http

import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.os.IBinder
import android.support.v7.app.NotificationCompat
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.MainActivity
import ch.rmy.android.http_shortcuts.realm.Controller
import ch.rmy.android.http_shortcuts.utils.Connectivity
import ch.rmy.android.http_shortcuts.utils.IntentUtil
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
                val pendingExecution = pendingExecutions.first()
                val id = pendingExecution.shortcutId
                val variableValues = HashMap<String, String>()
                for (resolvedVariable in pendingExecution.resolvedVariables!!) {
                    variableValues.put(resolvedVariable.key!!, resolvedVariable.value!!)
                }

                controller.removePendingExecution(pendingExecution)

                try {
                    Thread.sleep(INITIAL_DELAY.toLong())
                    executeShortcut(id, variableValues)
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

    private fun updateNotification(size: Int) {
        val intent = Intent(context, MainActivity::class.java)
        val builder = NotificationCompat.Builder(context)
                .setContentTitle(getString(R.string.title_shortcuts_pending))
                .setContentText(context.resources.getQuantityString(R.plurals.message_shortcuts_pending, size, size))
                .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.ic_launcher))
                .setContentIntent(PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT))
                .setSmallIcon(R.drawable.ic_waiting)
        startForeground(NOTIFICATION_ID, builder.build())
    }

    private fun executeShortcut(id: Long, variableValues: HashMap<String, String>) {
        val shortcutIntent = IntentUtil.createIntent(context, id, variableValues)
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

        fun start(context: Context) {
            context.startService(Intent(context, ExecutionService::class.java))
        }

    }

}

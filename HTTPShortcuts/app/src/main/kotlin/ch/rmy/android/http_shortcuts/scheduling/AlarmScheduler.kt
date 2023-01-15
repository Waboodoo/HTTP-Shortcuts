package ch.rmy.android.http_shortcuts.scheduling

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import android.os.SystemClock
import ch.rmy.android.http_shortcuts.data.domains.pending_executions.ExecutionId
import javax.inject.Inject

class AlarmScheduler
@Inject
constructor(
    private val context: Context,
) {
    private val alarmManager: AlarmManager
        get() = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun createAlarm(id: ExecutionId, requestCode: Int, delay: Long) {
        val pendingIntent = getPendingIntent(id, requestCode)
        alarmManager.set(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + delay,
            pendingIntent,
        )
    }

    private fun getPendingIntent(id: ExecutionId, requestCode: Int): PendingIntent {
        val intent = ExecutionBroadcastReceiver.createIntent(context, id, requestCode)
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0) or PendingIntent.FLAG_CANCEL_CURRENT,
        )
    }

    fun cancelAlarm(id: ExecutionId, requestCode: Int) {
        alarmManager.cancel(getPendingIntent(id, requestCode))
    }
}

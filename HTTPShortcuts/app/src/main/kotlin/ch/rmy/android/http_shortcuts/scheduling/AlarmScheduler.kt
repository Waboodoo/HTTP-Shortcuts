package ch.rmy.android.http_shortcuts.scheduling

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.os.SystemClock
import ch.rmy.android.http_shortcuts.data.domains.pending_executions.ExecutionId
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

class AlarmScheduler
@Inject
constructor(
    private val context: Context,
) {
    private val alarmManager: AlarmManager
        get() = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun createAlarm(id: ExecutionId, requestCode: Int, delay: Duration) {
        val pendingIntent = getPendingIntent(id, requestCode)
        alarmManager.cancel(pendingIntent)
        alarmManager.set(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + delay.compensateInaccuracy().inWholeMilliseconds,
            pendingIntent,
        )
    }

    private fun getPendingIntent(id: ExecutionId, requestCode: Int): PendingIntent {
        val intent = ExecutionBroadcastReceiver.createIntent(context, id, requestCode)
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT,
        )
    }

    fun cancelAlarm(id: ExecutionId, requestCode: Int) {
        alarmManager.cancel(getPendingIntent(id, requestCode))
    }

    private fun Duration.compensateInaccuracy(): Duration =
        when {
            this <= 2.hours -> this * 0.85
            this <= 8.hours -> this * 0.95
            else -> this
        }
}

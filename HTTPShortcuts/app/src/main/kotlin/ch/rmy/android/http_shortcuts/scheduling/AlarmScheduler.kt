package ch.rmy.android.http_shortcuts.scheduling

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import ch.rmy.android.http_shortcuts.data.domains.pending_executions.ExecutionId
import java.time.Instant
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class AlarmScheduler
@Inject
constructor(
    private val context: Context,
) {
    private val alarmManager: AlarmManager
        get() = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun createAlarm(id: ExecutionId, requestCode: Int, time: Instant, interval: Duration) {
        val pendingIntent = getPendingIntent(id, requestCode)
        val windowSize = (interval * 0.25)
            .coerceIn(10.minutes, 20.minutes)
        alarmManager.cancel(pendingIntent)
        alarmManager.setWindow(
            AlarmManager.RTC_WAKEUP,
            time.toEpochMilli() - (windowSize * 0.25).inWholeMilliseconds,
            windowSize.inWholeMilliseconds,
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
}

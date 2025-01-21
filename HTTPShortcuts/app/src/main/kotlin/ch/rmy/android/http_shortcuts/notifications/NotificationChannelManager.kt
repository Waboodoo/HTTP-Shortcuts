package ch.rmy.android.http_shortcuts.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.content.getSystemService
import ch.rmy.android.http_shortcuts.R
import javax.inject.Inject

class NotificationChannelManager
@Inject
constructor(
    private val context: Context,
) {
    private val notificationManager: NotificationManager?
        get() = context.getSystemService()

    fun createChannels() {
        notificationManager
            ?.createNotificationChannels(getChannels())
    }

    private fun getChannels() = listOf(
        NotificationChannel(
            NotificationChannelIds.SHORTCUT_EXECUTION,
            context.getString(R.string.notification_channel_shortcut_execution_title),
            NotificationManager.IMPORTANCE_LOW,
        ),
        NotificationChannel(
            NotificationChannelIds.SHORTCUT_RESULTS,
            context.getString(R.string.notification_channel_shortcut_result_title),
            NotificationManager.IMPORTANCE_DEFAULT,
        ),
    )
}

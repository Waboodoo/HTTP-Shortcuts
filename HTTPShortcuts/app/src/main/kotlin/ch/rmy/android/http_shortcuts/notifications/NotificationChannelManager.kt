package ch.rmy.android.http_shortcuts.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.content.getSystemService
import ch.rmy.android.http_shortcuts.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class NotificationChannelManager
@Inject
constructor(
    @ApplicationContext
    private val context: Context,
) {
    fun createChannels() {
        context.getSystemService<NotificationManager>()
            ?.createNotificationChannels(getChannels())
    }

    private fun getChannels() = listOf(
        NotificationChannel(
            NotificationChannelIds.SHORTCUT_EXECUTION,
            context.getString(R.string.notification_channel_shortcut_execution_title),
            NotificationManager.IMPORTANCE_LOW,
        ),
    )
}

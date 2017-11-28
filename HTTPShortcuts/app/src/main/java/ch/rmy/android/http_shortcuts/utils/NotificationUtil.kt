package ch.rmy.android.http_shortcuts.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import ch.rmy.android.http_shortcuts.R

object NotificationUtil {

    const val PENDING_SHORTCUTS_NOTIFICATION_CHANNEL = "pending"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val name = context.getString(R.string.name_pending_shortcuts_channel)
            val importance = NotificationManager.IMPORTANCE_MIN
            val channel = NotificationChannel(PENDING_SHORTCUTS_NOTIFICATION_CHANNEL, name, importance)
            notificationManager.createNotificationChannel(channel)
        }
    }

}

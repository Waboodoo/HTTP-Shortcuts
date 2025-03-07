package ch.rmy.android.http_shortcuts.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import ch.rmy.android.framework.extensions.runIf
import ch.rmy.android.framework.extensions.tryOrLog
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.main.MainActivity
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class Notifier
@Inject
constructor(
    private val context: Context,
    private val notificationChannelManager: NotificationChannelManager,
) {
    private val idCounter = AtomicInteger()

    private val notificationManager: NotificationManager?
        get() = context.getSystemService()

    fun showTextNotification(
        title: String,
        message: String?,
    ) {
        showNotification {
            setContentTitle(title)
                .setContentText(message)
                .runIf((message?.length ?: 0) > BIG_TEXT_LIMIT) {
                    setStyle(
                        NotificationCompat.BigTextStyle()
                            .setBigContentTitle(title)
                            .bigText(message),
                    )
                }
        }
    }

    suspend fun showImageNotification(
        title: String,
        image: Uri,
    ) {
        val bitmap = withContext(Dispatchers.IO) {
            context.contentResolver.openInputStream(image)
                ?.use {
                    BitmapFactory.decodeStream(it)
                }
        }
        try {
            showNotification {
                setContentTitle(title)
                    .setStyle(
                        NotificationCompat.BigPictureStyle()
                            .bigPicture(bitmap)
                            .setBigContentTitle(title),
                    )
            }
        } finally {
            bitmap?.recycle()
        }
    }

    private fun showNotification(content: NotificationCompat.Builder.() -> Unit) {
        notificationChannelManager.createChannels()
        tryOrLog {
            notificationManager?.notify(
                idCounter.incrementAndGet(),
                NotificationCompat.Builder(context, NotificationChannelIds.SHORTCUT_EXECUTION)
                    .apply(content)
                    .setSmallIcon(R.drawable.ic_quick_settings_tile)
                    .setAutoCancel(true)
                    .setContentIntent(
                        MainActivity.IntentBuilder()
                            .build(context)
                            .let { notificationIntent ->
                                PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
                            },
                    )
                    .build(),
            )
        }
    }

    companion object {
        private const val BIG_TEXT_LIMIT = 100
    }
}

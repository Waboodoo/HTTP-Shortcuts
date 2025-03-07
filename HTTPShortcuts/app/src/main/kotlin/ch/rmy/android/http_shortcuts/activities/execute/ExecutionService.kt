package ch.rmy.android.http_shortcuts.activities.execute

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import ch.rmy.android.framework.extensions.context
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.extensions.logInfo
import ch.rmy.android.framework.utils.UUIDUtils
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.ExecuteActivity.Companion.toExecutionParams
import ch.rmy.android.http_shortcuts.activities.main.MainActivity
import ch.rmy.android.http_shortcuts.activities.misc.host.HostActivity
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.notifications.NotificationChannelIds
import ch.rmy.android.http_shortcuts.notifications.NotificationChannelManager
import ch.rmy.android.http_shortcuts.utils.IconUtil
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ExecutionService : Service() {
    private val coroutineScope = CoroutineScope(Job())

    @Inject
    lateinit var executionFactory: ExecutionFactory

    @Inject
    lateinit var dialogHandler: ExecuteDialogHandler

    @Inject
    lateinit var shortcutRepository: ShortcutRepository

    @Inject
    lateinit var notificationChannelManager: NotificationChannelManager

    override fun onBind(intent: Intent?): IBinder? = null

    private val activeExecutions = AtomicInteger()
    private val activeShortcuts = ConcurrentHashMap<String, Shortcut>()

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val params = intent.toExecutionParams()
        val invocationId = UUIDUtils.newUUID()
        coroutineScope.launch {
            activeExecutions.incrementAndGet()
            val foregroundJob = launch {
                try {
                    val shortcut = shortcutRepository.getShortcutById(params.shortcutId)
                    activeShortcuts[invocationId] = shortcut
                } catch (_: NoSuchElementException) {
                    // this shouldn't happen, but if it does, the notification will just not show any details
                }
                startOrUpdateForegroundService()
            }
            val dialogJob = launch {
                dialogHandler.dialogState.collect { dialogState ->
                    logInfo("ExecutionService processing dialog")
                    if (dialogState != null) {
                        try {
                            val result = HostActivity.showDialog(context, dialogState)
                            logInfo("ExecutionService dialog result received")
                            dialogHandler.onDialogResult(result)
                        } catch (_: CancellationException) {
                            logInfo("ExecutionService dialog cancelled")
                            dialogHandler.onDialogDismissed()
                        }
                    }
                }
            }

            try {
                val execution = executionFactory.createExecution(params, dialogHandler)
                execution.execute().collect()
                logInfo("ExecutionService finished")
            } catch (_: CancellationException) {
                // Nothing to do here
            } catch (e: Throwable) {
                logException(e)
            } finally {
                foregroundJob.join()
                activeShortcuts.remove(invocationId)
                dialogJob.cancel()
            }
            if (activeExecutions.decrementAndGet() == 0) {
                activeShortcuts.clear()
                stopSelf()
            } else {
                startOrUpdateForegroundService()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.coroutineContext.cancelChildren()
    }

    private fun startOrUpdateForegroundService() {
        notificationChannelManager.createChannels()
        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            buildNotification(),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SHORT_SERVICE
            } else {
                0
            },
        )
    }

    private fun buildNotification(): Notification {
        val activeShortcuts = activeShortcuts.values
        val shortcut = activeShortcuts.firstOrNull()
            ?.takeIf { first -> activeShortcuts.all { it.id == first.id } }
        val title = shortcut?.name
        val counter = activeExecutions.get()
        val text = context.resources.getQuantityString(R.plurals.notification_shortcut_execution_text_multiple, counter, counter)
        return NotificationCompat.Builder(context, NotificationChannelIds.SHORTCUT_EXECUTION)
            .setContentTitle(title ?: text)
            .setContentText(text.takeIf { title != null })
            .setNumber(counter)
            .setLargeIcon(shortcut?.icon?.let { IconUtil.getIcon(context, it) })
            .setSmallIcon(R.drawable.ic_quick_settings_tile)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_DEFERRED)
            .setLocalOnly(true)
            .setShowWhen(false)
            .setContentIntent(
                // TODO: What should happen when the notification is clicked? Should there be a way to cancel execution?
                MainActivity.IntentBuilder()
                    .build(context)
                    .let { notificationIntent ->
                        PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
                    },
            )
            .build()
    }

    companion object {
        private const val NOTIFICATION_ID = 7
    }
}

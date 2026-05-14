package ch.rmy.android.http_shortcuts.activities.execute

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import ch.rmy.android.framework.extensions.context
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.extensions.logInfo
import ch.rmy.android.framework.extensions.runIf
import ch.rmy.android.framework.extensions.runIfNotNull
import ch.rmy.android.framework.utils.UUIDUtils
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.ExecuteActivity.Companion.toExecutionParams
import ch.rmy.android.http_shortcuts.activities.execute.models.ExecutionStatus
import ch.rmy.android.http_shortcuts.activities.main.MainActivity
import ch.rmy.android.http_shortcuts.activities.misc.host.HostActivity
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.http.UploadProgress
import ch.rmy.android.http_shortcuts.notifications.NotificationChannelIds
import ch.rmy.android.http_shortcuts.notifications.NotificationChannelManager
import ch.rmy.android.http_shortcuts.utils.IconUtil
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ExecutionService : LifecycleService() {
    @Inject
    lateinit var executionFactory: ExecutionFactory

    @Inject
    lateinit var dialogHandler: ExecuteDialogHandler

    @Inject
    lateinit var shortcutRepository: ShortcutRepository

    @Inject
    lateinit var notificationChannelManager: NotificationChannelManager

    private val activeExecutions = AtomicInteger()
    private val activeShortcuts = ConcurrentHashMap<String, Shortcut>()

    private var channelsCreated = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        val params = intent!!.toExecutionParams()
        val invocationId = UUIDUtils.newUUID()
        logInfo("ExecutionService: onStartCommand")
        lifecycleScope.launch {
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
                execution.execute().collect { status ->
                    if (status is ExecutionStatus.ProgressUpdate) {
                        startOrUpdateForegroundService(status.progress)
                    }
                }
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
                ServiceCompat.stopForeground(this@ExecutionService, ServiceCompat.STOP_FOREGROUND_REMOVE)
                stopSelf()
            } else {
                startOrUpdateForegroundService()
            }
        }
        return START_NOT_STICKY
    }

    private fun startOrUpdateForegroundService(progress: UploadProgress? = null) {
        if (!channelsCreated) {
            notificationChannelManager.createChannels()
            channelsCreated = true
        }
        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            buildNotification(progress),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SHORT_SERVICE
            } else {
                0
            },
        )
    }

    private fun buildNotification(progress: UploadProgress?): Notification {
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
            .runIfNotNull(progress) { progress ->
                runIf(counter == 1) {
                    setProgress(1000, (progress.progress * 1000).toInt(), false)
                }
            }
            .setContentIntent(
                // TODO(???): What should happen when the notification is clicked? Should there be a way to cancel execution?
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

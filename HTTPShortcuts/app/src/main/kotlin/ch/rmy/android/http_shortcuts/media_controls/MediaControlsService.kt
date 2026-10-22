package ch.rmy.android.http_shortcuts.media_controls

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.content.res.Configuration
import android.graphics.drawable.Icon
import android.media.MediaMetadata
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.view.KeyEvent
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import ch.rmy.android.framework.extensions.getParcelable
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.execute.ExecutionStarter
import ch.rmy.android.http_shortcuts.activities.main.MainActivity
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryRepository
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.enums.ShortcutTriggerType
import ch.rmy.android.http_shortcuts.data.settings.UserPreferences
import ch.rmy.android.http_shortcuts.notifications.NotificationChannelIds
import ch.rmy.android.http_shortcuts.notifications.NotificationChannelManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MediaControlsService : LifecycleService() {

    @Inject
    lateinit var shortcutRepository: ShortcutRepository

    @Inject
    lateinit var categoryRepository: CategoryRepository

    @Inject
    lateinit var executionStarter: ExecutionStarter

    @Inject
    lateinit var userPreferences: UserPreferences

    @Inject
    lateinit var notificationChannelManager: NotificationChannelManager

    @Inject
    lateinit var mediaControlExecutionTracker: MediaControlExecutionTracker

    private lateinit var mediaSession: MediaSession
    private val state = MediaControlState(MAX_PENDING_COMMANDS)
    private val silentAudioPlayer = SilentAudioPlayer()
    private var foregroundStarted = false
    private var triggerFeedbackStartedAtMs: Long? = null
    private var resumePlaybackJob: Job? = null
    private var updateNotificationJob: Job? = null

    private val triggerPending: Boolean
        get() = state.isExecuting || triggerFeedbackStartedAtMs != null

    override fun onCreate() {
        super.onCreate()
        notificationChannelManager.createChannels()
        mediaSession = MediaSession(this, MEDIA_SESSION_TAG).apply {
            setCallback(MediaSessionCallback())
            setSessionActivity(createContentIntent())
            setPlaybackToLocal(silentAudioPlayer.audioAttributes)
            configureMediaButtonReceiver(this)
            isActive = true
        }
        updateMediaSession()

        lifecycleScope.launch {
            combine(
                shortcutRepository.observeShortcuts(),
                categoryRepository.observeCategories(),
                userPreferences.observeMediaControlCategorySelection(),
            ) { shortcuts, categories, categorySelection ->
                val visibleShortcutsByCategory = shortcuts
                    .filterNot { it.hidden }
                    .groupBy { it.categoryId }
                val visibleCategories = categories
                    .filterNot { it.hidden }
                    .map { category ->
                        MediaShortcutCategory(
                            id = category.id,
                            name = category.name,
                            shortcuts = visibleShortcutsByCategory[category.id]
                                .orEmpty()
                                .map { shortcut ->
                                    MediaShortcutItem(
                                        id = shortcut.id,
                                        name = shortcut.name,
                                        description = shortcut.description,
                                    )
                                },
                        )
                    }
                MediaControlCatalog.create(
                    categories = visibleCategories,
                    includeAllCategories = categorySelection.includeAllCategories,
                    selectedCategoryIds = categorySelection.selectedCategoryIds,
                )
            }
                .collect { catalog ->
                    val pendingCommands = state.replaceCatalog(catalog)
                    updateMediaSession()
                    pendingCommands.forEach(::executeCommand)
                }
        }

        lifecycleScope.launch {
            mediaControlExecutionTracker.isExecuting.collect { isExecuting ->
                val wasTriggerPending = triggerPending
                state.setExecuting(isExecuting)
                if (isExecuting) {
                    resumePlaybackJob?.cancel()
                    resumePlaybackJob = null
                } else {
                    schedulePlaybackResume()
                }
                if (wasTriggerPending != triggerPending) {
                    updateMediaSession()
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        if (intent?.action == ACTION_STOP) {
            stopSimulation()
            return Service.START_NOT_STICKY
        }
        if (
            !userPreferences.mediaControlsEnabled ||
            !notificationChannelManager.isChannelEnabled(NotificationChannelIds.MEDIA_CONTROLS)
        ) {
            userPreferences.mediaControlsEnabled = false
            stopSelf()
            return Service.START_NOT_STICKY
        }

        startOrUpdateForeground()
        silentAudioPlayer.start()
        when (intent?.action) {
            Intent.ACTION_MEDIA_BUTTON -> intent.getParcelable<KeyEvent>(Intent.EXTRA_KEY_EVENT)
                ?.let { handleCommand(MediaControlCommand.MediaButton(it)) }
        }
        return Service.START_STICKY
    }

    private fun handleCommand(command: MediaControlCommand) {
        state.accept(command).forEach(::executeCommand)
    }

    private fun executeCommand(command: MediaControlCommand) {
        when (command) {
            is MediaControlCommand.MediaButton -> mediaSession.controller.dispatchMediaButtonEvent(command.event)
            MediaControlCommand.Next -> selectNext()
            MediaControlCommand.Trigger -> executeCurrentShortcut()
            MediaControlCommand.Previous -> selectPrevious()
        }
    }

    private fun selectPrevious() {
        if (triggerPending) {
            return
        }
        state.previous()
        updateMediaSession()
    }

    private fun selectNext() {
        if (triggerPending) {
            return
        }
        state.next()
        updateMediaSession()
    }

    private fun executeCurrentShortcut() {
        if (triggerPending) {
            return
        }
        when (val item = state.current ?: return) {
            MediaControlItem.ChangeCategory -> {
                startTriggerFeedback()
                state.changeCategory()
                updateMediaSession()
                schedulePlaybackResume()
            }
            is MediaControlItem.Shortcut -> {
                startTriggerFeedback()
                state.setExecuting(true)
                updateMediaSession()
                executionStarter.execute(
                    shortcutId = item.shortcut.id,
                    trigger = ShortcutTriggerType.MEDIA_CONTROLS,
                    forceForegroundService = true,
                )
            }
        }
    }

    private fun startTriggerFeedback() {
        triggerFeedbackStartedAtMs = SystemClock.elapsedRealtime()
        resumePlaybackJob?.cancel()
        resumePlaybackJob = null
    }

    private fun schedulePlaybackResume() {
        val feedbackStartedAtMs = triggerFeedbackStartedAtMs ?: return
        resumePlaybackJob?.cancel()
        resumePlaybackJob = lifecycleScope.launch {
            val feedbackDuration = SystemClock.elapsedRealtime() - feedbackStartedAtMs
            delay((MIN_TRIGGER_FEEDBACK_DURATION_MS - feedbackDuration).coerceAtLeast(0))
            triggerFeedbackStartedAtMs = null
            resumePlaybackJob = null
            updateMediaSession()
        }
    }

    private fun stopSimulation() {
        userPreferences.mediaControlsEnabled = false
        stopSelf()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        notificationChannelManager.createChannels()
        updateMediaSession()
    }

    @SuppressLint("MissingPermission")
    private fun updateMediaSession() {
        val item = state.current
        val title = item.getDisplayName()
        val categoryName = state.currentCategory?.name
        mediaSession.setMetadata(
            MediaMetadata.Builder()
                .putString(MediaMetadata.METADATA_KEY_TITLE, title)
                .putString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE, title)
                .putString(MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE, item.getDescription())
                // Some wearables show the artist instead of the album as their secondary label.
                .putString(MediaMetadata.METADATA_KEY_ARTIST, categoryName ?: getString(R.string.app_name))
                .putString(MediaMetadata.METADATA_KEY_ALBUM, categoryName)
                .putString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST, categoryName)
                .putLong(MediaMetadata.METADATA_KEY_NUM_TRACKS, state.itemCount.toLong())
                .putLong(MediaMetadata.METADATA_KEY_TRACK_NUMBER, state.selectedItemNumber.toLong())
                .build(),
        )
        mediaSession.setPlaybackState(
            PlaybackState.Builder()
                .setActions(PLAYBACK_ACTIONS)
                // Wearables such as Huawei watches require an active player. The brief paused state acknowledges a trigger.
                .setState(
                    if (triggerPending) PlaybackState.STATE_PAUSED else PlaybackState.STATE_PLAYING,
                    PlaybackState.PLAYBACK_POSITION_UNKNOWN,
                    if (triggerPending) 0f else 1f,
                )
                .build(),
        )

        if (foregroundStarted) {
            updateNotificationJob?.cancel()
            updateNotificationJob = lifecycleScope.launch {
                delay(NOTIFICATION_UPDATE_DELAY_MS)
                NotificationManagerCompat.from(this@MediaControlsService).notify(NOTIFICATION_ID, buildNotification())
                updateNotificationJob = null
            }
        }
    }

    private fun startOrUpdateForeground() {
        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            buildNotification(),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            } else {
                0
            },
        )
        foregroundStarted = true
    }

    private fun buildNotification(): Notification {
        val categoryName = state.currentCategory?.name
        val itemName = state.current.getDisplayName()
        return Notification.Builder(this, NotificationChannelIds.MEDIA_CONTROLS)
            .setContentTitle(getString(R.string.media_controls_notification_text))
            .setContentText(
                if (categoryName != null) {
                    getString(R.string.media_controls_category_item_pattern, categoryName, itemName)
                } else {
                    itemName
                },
            )
            .setSmallIcon(R.drawable.ic_quick_settings_tile)
            .setContentIntent(createContentIntent())
            .setCategory(Notification.CATEGORY_SERVICE)
            .setOngoing(true)
            .setShowWhen(false)
            .addExtras(
                Bundle().apply {
                    // Companion apps can observe session updates without turning this into a system media notification.
                    putParcelable(Notification.EXTRA_MEDIA_SESSION, mediaSession.sessionToken)
                },
            )
            .addAction(
                Notification.Action.Builder(
                    Icon.createWithResource(this, R.drawable.outline_power_settings_new_24),
                    getString(R.string.media_controls_stop),
                    createStopIntent(),
                ).build(),
            )
            .build()
    }

    private fun MediaControlItem?.getDisplayName() = when (this) {
        MediaControlItem.ChangeCategory -> getString(R.string.media_controls_change_category)
        is MediaControlItem.Shortcut -> shortcut.name.ifEmpty { getString(R.string.shortcut_safe_name) }
        null -> getString(R.string.media_controls_no_shortcuts)
    }

    private fun MediaControlItem?.getDescription() = when (this) {
        MediaControlItem.ChangeCategory -> getString(R.string.media_controls_change_category_summary)
        is MediaControlItem.Shortcut -> shortcut.description.takeIf { it.isNotEmpty() }
        null -> null
    }

    private fun createContentIntent(): PendingIntent =
        PendingIntent.getActivity(
            this,
            REQUEST_CODE_CONTENT,
            MainActivity.IntentBuilder().build(this),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    @SuppressLint("UnspecifiedImmutableFlag")
    @Suppress("DEPRECATION")
    private fun configureMediaButtonReceiver(mediaSession: MediaSession) {
        mediaSession.setFlags(
            MediaSession.FLAG_HANDLES_MEDIA_BUTTONS or
                MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS,
        )
        val receiver = ComponentName(this, MediaControlReceiver::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            mediaSession.setMediaButtonBroadcastReceiver(receiver)
        } else {
            mediaSession.setMediaButtonReceiver(
                PendingIntent.getBroadcast(
                    this,
                    REQUEST_CODE_MEDIA_BUTTON,
                    Intent(Intent.ACTION_MEDIA_BUTTON).setComponent(receiver),
                    PendingIntent.FLAG_UPDATE_CURRENT,
                ),
            )
        }
    }

    private fun createStopIntent(): PendingIntent =
        PendingIntent.getService(
            this,
            REQUEST_CODE_STOP,
            Intent(this, MediaControlsService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    override fun onDestroy() {
        foregroundStarted = false
        silentAudioPlayer.release()
        if (!userPreferences.mediaControlsEnabled) {
            clearMediaButtonReceiver(mediaSession)
        }
        mediaSession.isActive = false
        mediaSession.release()
        super.onDestroy()
    }

    @Suppress("DEPRECATION")
    private fun clearMediaButtonReceiver(mediaSession: MediaSession) {
        // setMediaButtonBroadcastReceiver(null) crashes on some Android 16 devices despite accepting a nullable value.
        mediaSession.setMediaButtonReceiver(null)
    }

    private inner class MediaSessionCallback : MediaSession.Callback() {

        override fun onMediaButtonEvent(mediaButtonIntent: Intent): Boolean {
            val event = mediaButtonIntent.getParcelable<KeyEvent>(Intent.EXTRA_KEY_EVENT)
                ?: return false
            if (event.action != KeyEvent.ACTION_DOWN || event.repeatCount != 0) {
                return true
            }
            return super.onMediaButtonEvent(mediaButtonIntent)
        }

        override fun onPlay() {
            handleCommand(MediaControlCommand.Trigger)
        }

        override fun onPause() {
            handleCommand(MediaControlCommand.Trigger)
        }

        override fun onStop() {
            stopSimulation()
        }

        override fun onRewind() {
            handleCommand(MediaControlCommand.Previous)
        }

        override fun onSkipToPrevious() {
            handleCommand(MediaControlCommand.Previous)
        }

        override fun onSkipToNext() {
            handleCommand(MediaControlCommand.Next)
        }

        override fun onFastForward() {
            handleCommand(MediaControlCommand.Next)
        }
    }

    companion object {
        private const val MEDIA_SESSION_TAG = "HTTP Shortcuts Media Controls"
        private const val NOTIFICATION_ID = 9
        private const val REQUEST_CODE_CONTENT = 0
        private const val REQUEST_CODE_STOP = 1
        private const val REQUEST_CODE_MEDIA_BUTTON = 2
        private const val MAX_PENDING_COMMANDS = 32
        private const val MIN_TRIGGER_FEEDBACK_DURATION_MS = 1_000L
        private const val NOTIFICATION_UPDATE_DELAY_MS = 100L
        private const val ACTION_STOP = "ch.rmy.android.http_shortcuts.media_controls.STOP"
        private const val PLAYBACK_ACTIONS =
            PlaybackState.ACTION_STOP or
                PlaybackState.ACTION_PLAY or
                PlaybackState.ACTION_PAUSE or
                PlaybackState.ACTION_PLAY_PAUSE or
                PlaybackState.ACTION_REWIND or
                PlaybackState.ACTION_FAST_FORWARD or
                PlaybackState.ACTION_SKIP_TO_PREVIOUS or
                PlaybackState.ACTION_SKIP_TO_NEXT

        fun start(context: Context, mediaButtonEvent: KeyEvent? = null) {
            context.startForegroundService(
                Intent(context, MediaControlsService::class.java).apply {
                    if (mediaButtonEvent != null) {
                        action = Intent.ACTION_MEDIA_BUTTON
                        putExtra(Intent.EXTRA_KEY_EVENT, mediaButtonEvent)
                    }
                },
            )
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, MediaControlsService::class.java))
        }
    }
}

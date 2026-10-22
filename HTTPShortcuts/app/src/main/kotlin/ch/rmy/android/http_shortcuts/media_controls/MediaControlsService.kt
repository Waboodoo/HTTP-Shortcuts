package ch.rmy.android.http_shortcuts.media_controls

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.drawable.Icon
import android.media.MediaMetadata
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Build
import androidx.core.app.ServiceCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.execute.ExecutionStarter
import ch.rmy.android.http_shortcuts.activities.main.MainActivity
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryRepository
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutRepository
import ch.rmy.android.http_shortcuts.data.enums.ShortcutTriggerType
import ch.rmy.android.http_shortcuts.notifications.NotificationChannelIds
import ch.rmy.android.http_shortcuts.notifications.NotificationChannelManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MediaControlsService : LifecycleService() {

    @Inject
    lateinit var categoryRepository: CategoryRepository

    @Inject
    lateinit var shortcutRepository: ShortcutRepository

    @Inject
    lateinit var executionStarter: ExecutionStarter

    @Inject
    lateinit var notificationChannelManager: NotificationChannelManager

    private val shortcutQueue = MediaShortcutQueue()
    private lateinit var mediaSession: MediaSession
    private var isForeground = false

    override fun onCreate() {
        super.onCreate()
        notificationChannelManager.createChannels()
        mediaSession = MediaSession(this, MEDIA_SESSION_TAG).apply {
            setCallback(mediaSessionCallback)
            setSessionActivity(createOpenAppPendingIntent())
            setPlaybackState(createPlaybackState())
            isActive = true
        }
        observeShortcuts()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        when (intent?.action) {
            ACTION_EXECUTE -> executeSelectedShortcut()
            ACTION_NEXT -> selectNextShortcut()
            ACTION_PREVIOUS -> selectPreviousShortcut()
        }
        startOrUpdateForegroundService()
        return Service.START_STICKY
    }

    override fun onDestroy() {
        mediaSession.isActive = false
        mediaSession.release()
        super.onDestroy()
    }

    private fun observeShortcuts() {
        lifecycleScope.launch {
            combine(
                categoryRepository.observeCategories(),
                shortcutRepository.observeShortcuts(),
            ) { categories, shortcuts ->
                categories
                    .filterNot { it.hidden }
                    .flatMap { category ->
                        shortcuts
                            .filter { it.categoryId == category.id && !it.hidden }
                            .map { shortcut ->
                                MediaShortcutItem(
                                    id = shortcut.id,
                                    name = shortcut.name,
                                    categoryName = category.name,
                                    description = shortcut.description,
                                )
                            }
                    }
            }
                .distinctUntilChanged()
                .collect { shortcuts ->
                    shortcutQueue.update(shortcuts)
                    updateMediaSession()
                }
        }
    }

    private fun selectNextShortcut() {
        shortcutQueue.selectNext()
        updateMediaSession()
    }

    private fun selectPreviousShortcut() {
        shortcutQueue.selectPrevious()
        updateMediaSession()
    }

    private fun executeSelectedShortcut() {
        shortcutQueue.selectedItem?.let { shortcut ->
            executionStarter.execute(
                shortcutId = shortcut.id,
                trigger = ShortcutTriggerType.MEDIA_CONTROLS,
            )
        }
    }

    private fun updateMediaSession() {
        val selectedShortcut = shortcutQueue.selectedItem
        mediaSession.setMetadata(
            MediaMetadata.Builder()
                .putString(
                    MediaMetadata.METADATA_KEY_TITLE,
                    selectedShortcut?.name ?: getString(R.string.media_controls_no_shortcuts),
                )
                .putString(MediaMetadata.METADATA_KEY_ARTIST, selectedShortcut?.categoryName.orEmpty())
                .putString(MediaMetadata.METADATA_KEY_DISPLAY_DESCRIPTION, selectedShortcut?.description.orEmpty())
                .build(),
        )
        mediaSession.setPlaybackState(createPlaybackState())
        if (isForeground) {
            startOrUpdateForegroundService()
        }
    }

    private fun createPlaybackState(): PlaybackState =
        PlaybackState.Builder()
            .setActions(
                PlaybackState.ACTION_PLAY or
                    PlaybackState.ACTION_PAUSE or
                    PlaybackState.ACTION_PLAY_PAUSE or
                    PlaybackState.ACTION_SKIP_TO_NEXT or
                    PlaybackState.ACTION_SKIP_TO_PREVIOUS,
            )
            .setState(PlaybackState.STATE_PLAYING, PlaybackState.PLAYBACK_POSITION_UNKNOWN, 1f)
            .build()

    private fun startOrUpdateForegroundService() {
        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            createNotification(),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            } else {
                0
            },
        )
        isForeground = true
    }

    private fun createNotification(): Notification =
        Notification.Builder(this, NotificationChannelIds.MEDIA_CONTROLS)
            .setSmallIcon(R.drawable.ic_quick_settings_tile)
            .setContentTitle(shortcutQueue.selectedItem?.name ?: getString(R.string.media_controls_notification_title))
            .setContentText(shortcutQueue.selectedItem?.categoryName ?: getString(R.string.media_controls_no_shortcuts))
            .setContentIntent(createOpenAppPendingIntent())
            .setCategory(Notification.CATEGORY_TRANSPORT)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setShowWhen(false)
            .addAction(
                Notification.Action.Builder(
                    Icon.createWithResource(ANDROID_PACKAGE_NAME, android.R.drawable.ic_media_previous),
                    getString(R.string.media_controls_previous),
                    createServicePendingIntent(ACTION_PREVIOUS, REQUEST_CODE_PREVIOUS),
                ).build(),
            )
            .addAction(
                Notification.Action.Builder(
                    Icon.createWithResource(ANDROID_PACKAGE_NAME, android.R.drawable.ic_media_play),
                    getString(R.string.media_controls_execute),
                    createServicePendingIntent(ACTION_EXECUTE, REQUEST_CODE_EXECUTE),
                ).build(),
            )
            .addAction(
                Notification.Action.Builder(
                    Icon.createWithResource(ANDROID_PACKAGE_NAME, android.R.drawable.ic_media_next),
                    getString(R.string.media_controls_next),
                    createServicePendingIntent(ACTION_NEXT, REQUEST_CODE_NEXT),
                ).build(),
            )
            .setStyle(
                Notification.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
                    .setShowActionsInCompactView(0, 1, 2),
            )
            .build()

    private fun createOpenAppPendingIntent(): PendingIntent =
        MainActivity.IntentBuilder()
            .build(this)
            .let { intent ->
                PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            }

    private fun createServicePendingIntent(action: String, requestCode: Int): PendingIntent =
        PendingIntent.getForegroundService(
            this,
            requestCode,
            Intent(this, MediaControlsService::class.java).setAction(action),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

    private val mediaSessionCallback = object : MediaSession.Callback() {
        override fun onPlay() = executeSelectedShortcut()

        override fun onPause() = executeSelectedShortcut()

        override fun onSkipToNext() = selectNextShortcut()

        override fun onSkipToPrevious() = selectPreviousShortcut()
    }

    companion object {
        private const val MEDIA_SESSION_TAG = "HTTP Shortcuts"
        private const val ANDROID_PACKAGE_NAME = "android"
        private const val NOTIFICATION_ID = 8

        private const val ACTION_EXECUTE = "ch.rmy.android.http_shortcuts.media_controls.EXECUTE"
        private const val ACTION_NEXT = "ch.rmy.android.http_shortcuts.media_controls.NEXT"
        private const val ACTION_PREVIOUS = "ch.rmy.android.http_shortcuts.media_controls.PREVIOUS"

        private const val REQUEST_CODE_EXECUTE = 1
        private const val REQUEST_CODE_NEXT = 2
        private const val REQUEST_CODE_PREVIOUS = 3
    }
}

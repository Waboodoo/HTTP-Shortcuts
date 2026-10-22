package ch.rmy.android.http_shortcuts.media_controls

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.view.KeyEvent
import ch.rmy.android.framework.extensions.getParcelable
import ch.rmy.android.framework.extensions.tryOrLog
import ch.rmy.android.http_shortcuts.data.settings.UserPreferences
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MediaControlReceiver : BroadcastReceiver() {

    @Inject
    lateinit var userPreferences: UserPreferences

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_MEDIA_BUTTON || !userPreferences.mediaControlsEnabled) {
            return
        }
        val mediaButtonEvent = intent.getParcelable<KeyEvent>(Intent.EXTRA_KEY_EVENT)
            ?: return
        if (mediaButtonEvent.action != KeyEvent.ACTION_DOWN || mediaButtonEvent.repeatCount != 0) {
            return
        }
        tryOrLog {
            MediaControlsService.start(context, mediaButtonEvent)
        }
    }
}

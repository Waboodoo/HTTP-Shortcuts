package ch.rmy.android.http_shortcuts.media_controls

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.http_shortcuts.data.settings.UserPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaControlsController
@Inject
constructor(
    context: Context,
    private val userPreferences: UserPreferences,
) {
    private val context = context.applicationContext

    fun applyPreference() {
        setEnabled(userPreferences.mediaControlsEnabled)
    }

    fun setEnabled(enabled: Boolean) {
        val intent = Intent(context, MediaControlsService::class.java)
        try {
            if (enabled) {
                ContextCompat.startForegroundService(context, intent)
            } else {
                context.stopService(intent)
            }
        } catch (e: RuntimeException) {
            logException(e)
        }
    }
}

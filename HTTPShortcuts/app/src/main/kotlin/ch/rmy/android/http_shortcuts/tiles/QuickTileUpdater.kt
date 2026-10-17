package ch.rmy.android.http_shortcuts.tiles

import android.content.ComponentName
import android.content.Context
import android.service.quicksettings.TileService
import ch.rmy.android.framework.extensions.logException
import javax.inject.Inject

class QuickTileUpdater
@Inject
constructor(
    private val context: Context,
) {
    fun update() {
        try {
            TileService.requestListeningState(context, ComponentName(context, QuickTileService::class.java))
        } catch (e: Exception) {
            if (e is IllegalArgumentException && e.message == "User 0 is not the current user.") {
                return
            }
            logException(e)
        }
    }
}

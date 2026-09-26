package ch.rmy.android.http_shortcuts.tiles

import android.content.ComponentName
import android.content.Context
import android.service.quicksettings.TileService
import ch.rmy.android.framework.extensions.tryOrLog
import javax.inject.Inject

class QuickTileUpdater
@Inject
constructor(
    private val context: Context,
) {
    fun update() {
        tryOrLog {
            TileService.requestListeningState(context, ComponentName(context, QuickTileService::class.java))
        }
    }
}

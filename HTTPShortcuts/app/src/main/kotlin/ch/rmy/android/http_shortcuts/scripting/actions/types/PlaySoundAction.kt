package ch.rmy.android.http_shortcuts.scripting.actions.types

import android.media.RingtoneManager
import android.net.Uri
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext

class PlaySoundAction(private val soundUri: Uri?) : BaseAction() {

    override suspend fun execute(executionContext: ExecutionContext) {
        val uri = soundUri ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        RingtoneManager.getRingtone(executionContext.context, uri)
            ?.play()
    }
}

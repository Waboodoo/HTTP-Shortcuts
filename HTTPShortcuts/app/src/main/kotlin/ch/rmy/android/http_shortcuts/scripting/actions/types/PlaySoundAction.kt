package ch.rmy.android.http_shortcuts.scripting.actions.types

import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import javax.inject.Inject

class PlaySoundAction
@Inject
constructor(
    private val context: Context,
) : Action<PlaySoundAction.Params> {
    override suspend fun Params.execute(executionContext: ExecutionContext) {
        val uri = soundUri ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        RingtoneManager.getRingtone(context, uri)
            ?.play()
    }

    data class Params(
        val soundUri: Uri?,
    )
}

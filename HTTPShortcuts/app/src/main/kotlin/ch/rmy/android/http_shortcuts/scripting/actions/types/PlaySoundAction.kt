package ch.rmy.android.http_shortcuts.scripting.actions.types

import android.media.RingtoneManager
import android.net.Uri
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import io.reactivex.Completable

class PlaySoundAction(private val soundUri: Uri?) : BaseAction() {

    override fun execute(executionContext: ExecutionContext) = Completable.fromAction {
        val uri = soundUri ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        RingtoneManager.getRingtone(executionContext.context, uri)
            ?.play()
    }

}

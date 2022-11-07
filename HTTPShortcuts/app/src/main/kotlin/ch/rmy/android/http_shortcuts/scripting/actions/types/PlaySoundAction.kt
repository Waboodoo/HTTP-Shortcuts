package ch.rmy.android.http_shortcuts.scripting.actions.types

import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import ch.rmy.android.http_shortcuts.dagger.ApplicationComponent
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import javax.inject.Inject

class PlaySoundAction(private val soundUri: Uri?) : BaseAction() {

    @Inject
    lateinit var context: Context

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }

    override suspend fun execute(executionContext: ExecutionContext) {
        val uri = soundUri ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        RingtoneManager.getRingtone(context, uri)
            ?.play()
    }
}

package ch.rmy.android.http_shortcuts.scripting.actions.types

import androidx.core.net.toUri
import ch.rmy.android.framework.extensions.runIf
import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionDTO

class PlaySoundActionType : BaseActionType() {

    override val type = TYPE

    override fun fromDTO(actionDTO: ActionDTO) = PlaySoundAction(
        soundUri = actionDTO.getString(0)?.normalize()?.toUri(),
    )

    override fun getAlias() = ActionAlias(
        functionName = FUNCTION_NAME,
        parameters = 1,
    )

    companion object {
        private const val TYPE = "play_sound"
        private const val FUNCTION_NAME = "playSound"

        internal fun String.normalize(): String =
            runIf(!contains("://")) {
                CONTENT_PREFIX + this
            }

        const val CONTENT_PREFIX = "content://"
    }
}

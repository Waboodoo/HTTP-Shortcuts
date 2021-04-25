package ch.rmy.android.http_shortcuts.scripting.actions.types

import androidx.core.net.toUri
import ch.rmy.android.http_shortcuts.extensions.mapIf
import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionDTO

class PlaySoundActionType : BaseActionType() {

    override val type = TYPE

    override fun fromDTO(actionDTO: ActionDTO) = PlaySoundAction(
        soundUri = normalize(actionDTO[KEY_SOUND_URI])?.toUri(),
    )

    override fun getAlias() = ActionAlias(
        functionName = FUNCTION_NAME,
        parameters = listOf(KEY_SOUND_URI),
    )

    companion object {

        const val TYPE = "play_sound"
        const val FUNCTION_NAME = "playSound"

        const val KEY_SOUND_URI = "uri"

        private fun normalize(uriString: String?): String? =
            uriString?.mapIf(!uriString.contains("://")) {
                CONTENT_PREFIX + this
            }

        const val CONTENT_PREFIX = "content://"
    }

}

package ch.rmy.android.http_shortcuts.scripting.actions.types

import androidx.core.net.toUri
import ch.rmy.android.framework.extensions.runIf
import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionRunnable
import ch.rmy.android.scripting.JsFunctionArgs
import javax.inject.Inject

class PlaySoundActionType
@Inject
constructor(
    private val playSoundAction: PlaySoundAction,
) : ActionType {
    override val type = TYPE

    override fun getActionRunnable(args: JsFunctionArgs) =
        ActionRunnable(
            action = playSoundAction,
            params = PlaySoundAction.Params(
                soundUri = args.getString(0)?.normalize()?.toUri(),
            ),
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

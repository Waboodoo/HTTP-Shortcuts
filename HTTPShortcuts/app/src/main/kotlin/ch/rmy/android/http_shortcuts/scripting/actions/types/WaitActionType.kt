package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionRunnable
import ch.rmy.android.scripting.JsFunctionArgs
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

class WaitActionType
@Inject
constructor(
    private val waitAction: WaitAction,
) : ActionType {
    override val type = TYPE

    override fun getActionRunnable(args: JsFunctionArgs) =
        ActionRunnable(
            action = waitAction,
            params = WaitAction.Params(
                duration = (args.getInt(0)?.takeIf { it > 0 } ?: 0).milliseconds,
            ),
        )

    override fun getAlias() = ActionAlias(
        functionName = FUNCTION_NAME,
        functionNameAliases = setOf(FUNCTION_NAME_ALIAS),
        parameters = 1,
    )

    companion object {
        private const val TYPE = "wait"
        private const val FUNCTION_NAME = "wait"
        private const val FUNCTION_NAME_ALIAS = "sleep"
    }
}

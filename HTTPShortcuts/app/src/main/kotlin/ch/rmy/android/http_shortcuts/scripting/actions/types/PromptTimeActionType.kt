package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionRunnable
import ch.rmy.android.scripting.JsFunctionArgs
import javax.inject.Inject

class PromptTimeActionType
@Inject
constructor(
    private val promptTimeAction: PromptTimeAction,
) : ActionType {
    override val type = TYPE

    override fun getActionRunnable(args: JsFunctionArgs) =
        ActionRunnable(
            action = promptTimeAction,
            params = PromptTimeAction.Params(
                format = args.getString(0)?.takeUnlessEmpty(),
                initialTime = args.getString(1),
                title = args.getString(2)?.takeUnlessEmpty(),
            ),
        )

    override fun getAlias() = ActionAlias(
        functionName = FUNCTION_NAME,
        parameters = 3,
    )

    companion object {
        private const val TYPE = "prompt_time"
        private const val FUNCTION_NAME = "promptTime"
    }
}

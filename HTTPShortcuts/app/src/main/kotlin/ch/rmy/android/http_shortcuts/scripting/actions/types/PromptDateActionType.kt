package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionRunnable
import ch.rmy.android.scripting.JsFunctionArgs
import javax.inject.Inject

class PromptDateActionType
@Inject
constructor(
    private val promptDateAction: PromptDateAction,
) : ActionType {
    override val type = TYPE

    override fun getActionRunnable(args: JsFunctionArgs) =
        ActionRunnable(
            action = promptDateAction,
            params = PromptDateAction.Params(
                format = args.getString(0)?.takeUnlessEmpty(),
                initialDate = args.getString(1),
                title = args.getString(2)?.takeUnlessEmpty(),
            ),
        )

    override fun getAlias() = ActionAlias(
        functionName = FUNCTION_NAME,
        parameters = 3,
    )

    companion object {
        private const val TYPE = "prompt_date"
        private const val FUNCTION_NAME = "promptDate"
    }
}

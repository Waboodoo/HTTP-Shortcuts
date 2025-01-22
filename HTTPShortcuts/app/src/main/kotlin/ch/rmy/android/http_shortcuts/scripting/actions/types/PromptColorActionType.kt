package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionRunnable
import ch.rmy.android.scripting.JsFunctionArgs
import javax.inject.Inject

class PromptColorActionType
@Inject
constructor(
    private val promptColorAction: PromptColorAction,
) : ActionType {
    override val type = TYPE

    override fun getActionRunnable(args: JsFunctionArgs) =
        ActionRunnable(
            action = promptColorAction,
            params = PromptColorAction.Params(
                initialColor = args.getString(0)?.takeUnlessEmpty(),
                title = args.getString(1)?.takeUnlessEmpty(),
            ),
        )

    override fun getAlias() = ActionAlias(
        functionName = FUNCTION_NAME,
        parameters = 2,
    )

    companion object {
        private const val TYPE = "prompt_color"
        private const val FUNCTION_NAME = "promptColor"
    }
}

package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionData
import ch.rmy.android.http_shortcuts.scripting.actions.ActionRunnable
import javax.inject.Inject

class PromptColorActionType
@Inject
constructor(
    private val promptColorAction: PromptColorAction,
) : ActionType {
    override val type = TYPE

    override fun getActionRunnable(actionDTO: ActionData) =
        ActionRunnable(
            action = promptColorAction,
            params = PromptColorAction.Params(
                initialColor = actionDTO.getString(0)?.takeUnlessEmpty(),
            ),
        )

    override fun getAlias() = ActionAlias(
        functionName = FUNCTION_NAME,
        parameters = 1,
    )

    companion object {
        private const val TYPE = "prompt_color"
        private const val FUNCTION_NAME = "promptColor"
    }
}

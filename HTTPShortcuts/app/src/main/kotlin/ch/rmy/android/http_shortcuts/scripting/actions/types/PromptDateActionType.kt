package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionData
import ch.rmy.android.http_shortcuts.scripting.actions.ActionRunnable
import javax.inject.Inject

class PromptDateActionType
@Inject
constructor(
    private val promptDateAction: PromptDateAction,
) : ActionType {
    override val type = TYPE

    override fun getActionRunnable(actionDTO: ActionData) =
        ActionRunnable(
            action = promptDateAction,
            params = PromptDateAction.Params(
                format = actionDTO.getString(0)?.takeUnlessEmpty(),
                initialDate = actionDTO.getString(1),
            ),
        )

    override fun getAlias() = ActionAlias(
        functionName = FUNCTION_NAME,
        parameters = 2,
    )

    companion object {
        private const val TYPE = "prompt_date"
        private const val FUNCTION_NAME = "promptDate"
    }
}

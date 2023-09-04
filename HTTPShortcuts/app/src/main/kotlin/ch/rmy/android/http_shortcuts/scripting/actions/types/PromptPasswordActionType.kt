package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionData
import ch.rmy.android.http_shortcuts.scripting.actions.ActionRunnable
import javax.inject.Inject

class PromptPasswordActionType
@Inject
constructor(
    private val promptPasswordAction: PromptPasswordAction,
) : ActionType {
    override val type = TYPE

    override fun getActionRunnable(actionDTO: ActionData) =
        ActionRunnable(
            action = promptPasswordAction,
            params = PromptPasswordAction.Params(
                message = actionDTO.getString(0) ?: "",
                prefill = actionDTO.getString(1) ?: "",
            ),
        )

    override fun getAlias() = ActionAlias(
        functionName = FUNCTION_NAME,
        parameters = 2,
    )

    companion object {
        private const val TYPE = "prompt_password"
        private const val FUNCTION_NAME = "promptPassword"
    }
}

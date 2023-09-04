package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionData
import ch.rmy.android.http_shortcuts.scripting.actions.ActionRunnable
import javax.inject.Inject

class SendIntentActionType
@Inject
constructor(
    private val sendIntentAction: SendIntentAction,
) : ActionType {
    override val type = TYPE

    override fun getActionRunnable(actionDTO: ActionData) =
        ActionRunnable(
            action = sendIntentAction,
            params = SendIntentAction.Params(
                jsonData = actionDTO.getString(0) ?: "{}",
            ),
        )

    override fun getAlias() = ActionAlias(
        functionName = FUNCTION_NAME,
        parameters = 1,
    )

    companion object {
        private const val TYPE = "send_intent"
        private const val FUNCTION_NAME = "sendIntent"
    }
}

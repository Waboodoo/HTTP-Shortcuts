package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionRunnable
import ch.rmy.android.scripting.JsFunctionArgs
import javax.inject.Inject

class SetVariableActionType
@Inject
constructor(
    private val setVariableAction: SetVariableAction,
) : ActionType {
    override val type = TYPE

    override fun getActionRunnable(args: JsFunctionArgs) =
        ActionRunnable(
            action = setVariableAction,
            params = SetVariableAction.Params(
                variableKeyOrId = args.getString(0) ?: "",
                value = args.getString(1) ?: "",
                storeOnly = args.getBoolean(2) == true,
            ),
        )

    override fun getAlias() = ActionAlias(
        functionName = FUNCTION_NAME,
        parameters = 3,
    )

    companion object {
        private const val TYPE = "set_variable"
        private const val FUNCTION_NAME = "setVariable"
    }
}

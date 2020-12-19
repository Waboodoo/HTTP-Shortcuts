package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionDTO

class GetVariableActionType : BaseActionType() {

    override val type = TYPE

    override fun fromDTO(actionDTO: ActionDTO) = GetVariableAction(
        variableKeyOrId = actionDTO[KEY_VARIABLE] ?: "",
    )

    override fun getAlias() = ActionAlias(
        functionName = FUNCTION_NAME,
        parameters = listOf(KEY_VARIABLE),
    )

    companion object {

        const val TYPE = "get_variable"
        const val FUNCTION_NAME = "getVariable"

        const val KEY_VARIABLE = "variable"

    }

}
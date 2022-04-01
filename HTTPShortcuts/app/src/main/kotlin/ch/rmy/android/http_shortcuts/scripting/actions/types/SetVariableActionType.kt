package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionDTO

class SetVariableActionType : BaseActionType() {

    override val type = TYPE

    override fun fromDTO(actionDTO: ActionDTO) = SetVariableAction(
        variableKeyOrId = actionDTO.getString(0) ?: "",
        value = actionDTO.getString(1) ?: "",
    )

    override fun getAlias() = ActionAlias(
        functionName = FUNCTION_NAME,
        parameters = 2,
    )

    companion object {
        private const val TYPE = "set_variable"
        private const val FUNCTION_NAME = "setVariable"
    }
}

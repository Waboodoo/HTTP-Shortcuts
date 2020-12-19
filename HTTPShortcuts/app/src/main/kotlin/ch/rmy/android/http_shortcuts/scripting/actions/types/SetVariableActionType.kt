package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionDTO

class SetVariableActionType : BaseActionType() {

    override val type = TYPE

    override fun fromDTO(actionDTO: ActionDTO) = SetVariableAction(
        variableKeyOrId = actionDTO[KEY_VARIABLE] ?: "",
        value = actionDTO[KEY_VALUE] ?: "",
    )

    override fun getAlias() = ActionAlias(
        functionName = FUNCTION_NAME,
        parameters = listOf(KEY_VARIABLE, KEY_VALUE),
    )

    companion object {

        const val TYPE = "set_variable"
        const val FUNCTION_NAME = "setVariable"

        const val KEY_VARIABLE = "variable"
        const val KEY_VALUE = "value"

    }

}
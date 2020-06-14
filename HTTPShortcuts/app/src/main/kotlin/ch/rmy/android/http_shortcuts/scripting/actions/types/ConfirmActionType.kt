package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionDTO

class ConfirmActionType : BaseActionType() {

    override val type = TYPE

    override fun fromDTO(actionDTO: ActionDTO) = ConfirmAction(
        message = actionDTO[KEY_MESSAGE] ?: ""
    )

    override fun getAlias() = ActionAlias(
        functionName = FUNCTION_NAME,
        parameters = listOf(KEY_MESSAGE),
        returnType = ActionAlias.ReturnType.BOOLEAN
    )

    companion object {

        const val TYPE = "confirm"
        const val FUNCTION_NAME = "confirm"

        const val KEY_MESSAGE = "message"

    }

}
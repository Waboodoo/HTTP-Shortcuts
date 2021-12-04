package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionDTO

class ToStringActionType : BaseActionType() {

    override val type = TYPE

    override fun fromDTO(actionDTO: ActionDTO) = ToStringAction(
        data = actionDTO.getByteArray(KEY_DATA) ?: ByteArray(0),
    )

    override fun getAlias() = ActionAlias(
        functionName = FUNCTION_NAME,
        parameters = listOf(KEY_DATA),
    )

    companion object {

        const val TYPE = "to_string"
        const val FUNCTION_NAME = "toString"

        const val KEY_DATA = "data"
    }
}

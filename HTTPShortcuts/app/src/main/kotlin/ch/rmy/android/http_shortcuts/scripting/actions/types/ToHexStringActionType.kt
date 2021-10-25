package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionDTO

class ToHexStringActionType : BaseActionType() {

    override val type = TYPE

    override fun fromDTO(actionDTO: ActionDTO) = ToHexStringAction(
        data = actionDTO.getByteArray(KEY_DATA) ?: ByteArray(0),
    )

    override fun getAlias() = ActionAlias(
        functionName = FUNCTION_NAME,
        parameters = listOf(KEY_DATA),
    )

    companion object {

        const val TYPE = "to_hex_string"
        const val FUNCTION_NAME = "toHexString"

        const val KEY_DATA = "data"

    }

}

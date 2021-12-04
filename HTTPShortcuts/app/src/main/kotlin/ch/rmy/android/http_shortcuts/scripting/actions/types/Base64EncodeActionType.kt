package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionDTO

class Base64EncodeActionType : BaseActionType() {

    override val type = TYPE

    override fun fromDTO(actionDTO: ActionDTO) = Base64EncodeAction(
        text = actionDTO.getByteArray(KEY_TEXT) ?: ByteArray(0),
    )

    override fun getAlias() = ActionAlias(
        functionName = FUNCTION_NAME,
        parameters = listOf(KEY_TEXT),
        functionNameAliases = setOf("base64Encode", "btoa"),
    )

    companion object {

        const val TYPE = "base64encode"
        const val FUNCTION_NAME = "base64encode"

        const val KEY_TEXT = "text"
    }
}

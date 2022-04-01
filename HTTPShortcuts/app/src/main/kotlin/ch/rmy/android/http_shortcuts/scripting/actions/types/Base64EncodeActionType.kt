package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionDTO

class Base64EncodeActionType : BaseActionType() {

    override val type = TYPE

    override fun fromDTO(actionDTO: ActionDTO) = Base64EncodeAction(
        text = actionDTO.getByteArray(0) ?: ByteArray(0),
    )

    override fun getAlias() = ActionAlias(
        functionName = FUNCTION_NAME,
        functionNameAliases = setOf("base64Encode", "btoa"),
        parameters = 1,
    )

    companion object {
        private const val TYPE = "base64encode"
        private const val FUNCTION_NAME = "base64encode"
    }
}

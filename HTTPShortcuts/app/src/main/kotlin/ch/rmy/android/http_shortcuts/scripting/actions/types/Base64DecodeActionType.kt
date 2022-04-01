package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionDTO

class Base64DecodeActionType : BaseActionType() {

    override val type = TYPE

    override fun fromDTO(actionDTO: ActionDTO) = Base64DecodeAction(
        encoded = actionDTO.getString(0) ?: "",
    )

    override fun getAlias() = ActionAlias(
        functionName = FUNCTION_NAME,
        functionNameAliases = setOf("base64Decode", "atob"),
        parameters = 1,
    )

    companion object {
        private const val TYPE = "base64decode"
        private const val FUNCTION_NAME = "base64decode"
    }
}

package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionDTO

class Base64DecodeActionType : BaseActionType() {

    override val type = TYPE

    override fun fromDTO(actionDTO: ActionDTO) = Base64DecodeAction(
        encoded = actionDTO.getString(KEY_TEXT) ?: "",
    )

    override fun getAlias() = ActionAlias(
        functionName = FUNCTION_NAME,
        parameters = listOf(KEY_TEXT),
        functionNameAliases = setOf("base64Decode", "atob"),
    )

    companion object {

        const val TYPE = "base64decode"
        const val FUNCTION_NAME = "base64decode"

        const val KEY_TEXT = "text"

    }

}
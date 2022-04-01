package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionDTO

class OpenURLActionType : BaseActionType() {

    override val type = TYPE

    override fun fromDTO(actionDTO: ActionDTO) = OpenURLAction(
        url = actionDTO.getString(0) ?: "",
    )

    override fun getAlias() = ActionAlias(
        functionName = FUNCTION_NAME,
        functionNameAliases = setOf("openURL"),
        parameters = 1,
    )

    companion object {
        private const val TYPE = "open_url"
        private const val FUNCTION_NAME = "openUrl"
    }
}

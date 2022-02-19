package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionDTO

class OpenURLActionType : BaseActionType() {

    override val type = TYPE

    override fun fromDTO(actionDTO: ActionDTO) = OpenURLAction(
        url = actionDTO.getString(KEY_URL) ?: "",
    )

    override fun getAlias() = ActionAlias(
        functionName = FUNCTION_NAME,
        parameters = listOf(KEY_URL),
        functionNameAliases = setOf("openURL"),
    )

    companion object {

        const val TYPE = "open_url"
        const val FUNCTION_NAME = "openUrl"

        const val KEY_URL = "url"
    }
}

package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionDTO

class DialogActionType : BaseActionType() {

    override val type = TYPE

    override fun fromDTO(actionDTO: ActionDTO) = DialogAction(
        message = actionDTO.getString(KEY_TEXT) ?: "",
        title = actionDTO.getString(KEY_TITLE) ?: "",
    )

    override fun getAlias() = ActionAlias(
        functionName = FUNCTION_NAME,
        functionNameAliases = setOf(FUNCTION_NAME_ALIAS),
        parameters = listOf(KEY_TEXT, KEY_TITLE),
    )

    companion object {

        const val TYPE = "show_dialog"
        const val FUNCTION_NAME = "showDialog"
        const val FUNCTION_NAME_ALIAS = "alert"

        const val KEY_TEXT = "text"
        const val KEY_TITLE = "title"
    }
}

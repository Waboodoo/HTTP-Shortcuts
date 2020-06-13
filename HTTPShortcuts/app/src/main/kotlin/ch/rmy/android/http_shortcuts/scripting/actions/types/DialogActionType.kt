package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionDTO

class DialogActionType : BaseActionType() {

    override val type = TYPE

    override fun fromDTO(actionDTO: ActionDTO) = DialogAction(actionDTO.data)

    override fun getAlias() = ActionAlias(
        functionName = FUNCTION_NAME,
        parameters = listOf(DialogAction.KEY_TEXT, DialogAction.KEY_TITLE)
    )

    companion object {

        const val TYPE = "show_dialog"
        const val FUNCTION_NAME = "showDialog"

    }

}
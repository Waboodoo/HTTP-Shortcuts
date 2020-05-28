package ch.rmy.android.http_shortcuts.scripting.actions.types


import android.content.Context
import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionDTO

class DialogActionType(context: Context) : BaseActionType(context) {

    override val type = TYPE

    override fun fromDTO(actionDTO: ActionDTO) = DialogAction(this, actionDTO.data)

    override fun getAlias() = ActionAlias(
        functionName = "showDialog",
        parameters = listOf(DialogAction.KEY_TEXT, DialogAction.KEY_TITLE)
    )

    companion object {

        const val TYPE = "show_dialog"

    }

}
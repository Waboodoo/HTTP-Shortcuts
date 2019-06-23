package ch.rmy.android.http_shortcuts.actions.types


import android.content.Context
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.actions.ActionDTO
import ch.rmy.android.http_shortcuts.scripting.ActionAlias

class DialogActionType(context: Context) : BaseActionType(context) {

    override val type = TYPE

    override val title: String = context.getString(R.string.action_type_dialog_title)

    override fun fromDTO(actionDTO: ActionDTO) = DialogAction(this, actionDTO.data)

    override fun getAlias() = ActionAlias(
        functionName = "showDialog",
        parameters = listOf(DialogAction.KEY_TEXT, DialogAction.KEY_TITLE)
    )

    companion object {

        const val TYPE = "show_dialog"

    }

}
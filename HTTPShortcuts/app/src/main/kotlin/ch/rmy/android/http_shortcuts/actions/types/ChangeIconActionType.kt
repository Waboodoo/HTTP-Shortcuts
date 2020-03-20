package ch.rmy.android.http_shortcuts.actions.types

import android.content.Context
import ch.rmy.android.http_shortcuts.actions.ActionDTO
import ch.rmy.android.http_shortcuts.scripting.ActionAlias

class ChangeIconActionType(context: Context) : BaseActionType(context) {

    override val type = TYPE

    override fun fromDTO(actionDTO: ActionDTO) = ChangeIconAction(this, actionDTO.data)

    override fun getAlias() = ActionAlias(
        functionName = "changeIcon",
        parameters = listOf(ChangeIconAction.KEY_SHORTCUT_NAME_OR_ID, ChangeIconAction.KEY_ICON)
    )

    companion object {

        const val TYPE = "change_icon"

    }

}
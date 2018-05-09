package ch.rmy.android.http_shortcuts.actions.types

import android.content.Context
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.actions.ActionDTO

class SetVariableActionType(context: Context) : BaseActionType(context) {

    override val type = TYPE

    override val title: String = context.getString(R.string.action_type_set_variable_title)

    override fun fromDTO(actionDTO: ActionDTO) = SetVariableAction(actionDTO.id, this, actionDTO.data)

    companion object {

        const val TYPE = "set_variable"

    }

}
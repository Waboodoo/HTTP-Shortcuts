package ch.rmy.android.http_shortcuts.actions.types

import android.content.Context
import ch.rmy.android.http_shortcuts.actions.ActionDTO

@Deprecated("Will be removed eventually")
class SetVariableActionType(context: Context) : BaseActionType(context) {

    override val type = TYPE

    override val title: String = "Set Variable"

    override fun fromDTO(actionDTO: ActionDTO) = SetVariableAction(this, actionDTO.data)

    companion object {

        const val TYPE = "set_variable"

    }

}
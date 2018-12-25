package ch.rmy.android.http_shortcuts.actions.types

import android.content.Context
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.actions.ActionDTO

class UnknownActionType(context: Context) : BaseActionType(context) {

    override val type = ""

    override val title: String = context.getString(R.string.action_type_unknown_title)

    override fun fromDTO(actionDTO: ActionDTO) = UnknownAction(actionDTO.id, this, actionDTO.data)
}
package ch.rmy.android.http_shortcuts.actions.types

import android.content.Context
import ch.rmy.android.http_shortcuts.actions.ActionDTO

class UnknownActionType(context: Context) : BaseActionType(context) {

    override val type = ""

    override fun fromDTO(actionDTO: ActionDTO) = UnknownAction(this, actionDTO.data)
}
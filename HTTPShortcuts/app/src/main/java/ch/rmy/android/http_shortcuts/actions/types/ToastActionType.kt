package ch.rmy.android.http_shortcuts.actions.types

import android.content.Context
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.actions.ActionDTO

class ToastActionType(context: Context) : BaseActionType(context) {

    override val type = TYPE

    override val title: String = context.getString(R.string.action_type_toast_title)

    override fun fromDTO(actionDTO: ActionDTO) = ToastAction(actionDTO.id, this, actionDTO.data)

    companion object {

        const val TYPE = "show_toast"

    }

}
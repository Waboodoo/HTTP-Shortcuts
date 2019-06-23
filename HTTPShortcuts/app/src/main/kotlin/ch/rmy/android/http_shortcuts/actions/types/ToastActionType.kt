package ch.rmy.android.http_shortcuts.actions.types

import android.content.Context
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.actions.ActionDTO
import ch.rmy.android.http_shortcuts.scripting.ActionAlias

class ToastActionType(context: Context) : BaseActionType(context) {

    override val type = TYPE

    override val title: String = context.getString(R.string.action_type_toast_title)

    override fun fromDTO(actionDTO: ActionDTO) = ToastAction(this, actionDTO.data)

    override fun getAlias() = ActionAlias(
        functionName = "showToast",
        parameters = listOf(ToastAction.KEY_TEXT)
    )

    companion object {

        const val TYPE = "show_toast"

    }

}
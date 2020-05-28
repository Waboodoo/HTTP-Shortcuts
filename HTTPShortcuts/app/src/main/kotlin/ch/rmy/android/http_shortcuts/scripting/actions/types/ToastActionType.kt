package ch.rmy.android.http_shortcuts.scripting.actions.types

import android.content.Context
import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionDTO

class ToastActionType(context: Context) : BaseActionType(context) {

    override val type = TYPE

    override fun fromDTO(actionDTO: ActionDTO) = ToastAction(this, actionDTO.data)

    override fun getAlias() = ActionAlias(
        functionName = "showToast",
        parameters = listOf(ToastAction.KEY_TEXT)
    )

    companion object {

        const val TYPE = "show_toast"

    }

}
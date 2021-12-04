package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionDTO

class ToastActionType : BaseActionType() {

    override val type = TYPE

    override fun fromDTO(actionDTO: ActionDTO) = ToastAction(
        message = actionDTO.getString(KEY_TEXT) ?: "",
    )

    override fun getAlias() = ActionAlias(
        functionName = FUNCTION_NAME,
        parameters = listOf(KEY_TEXT),
    )

    companion object {

        const val TYPE = "show_toast"
        const val FUNCTION_NAME = "showToast"

        const val KEY_TEXT = "text"
    }
}

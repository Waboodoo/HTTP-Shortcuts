package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionDTO

class SendIntentActionType : BaseActionType() {

    override val type = TYPE

    override fun fromDTO(actionDTO: ActionDTO) = SendIntentAction(
        jsonData = actionDTO[KEY_DATA] ?: "{}",
    )

    override fun getAlias() = ActionAlias(
        functionName = FUNCTION_NAME,
        parameters = listOf(KEY_DATA),
    )

    companion object {

        const val TYPE = "send_intent"
        const val FUNCTION_NAME = "sendIntent"

        const val KEY_DATA = "data"

    }

}

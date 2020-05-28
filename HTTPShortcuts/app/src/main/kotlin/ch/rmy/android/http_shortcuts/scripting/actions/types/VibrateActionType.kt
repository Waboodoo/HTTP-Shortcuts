package ch.rmy.android.http_shortcuts.scripting.actions.types

import android.content.Context
import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionDTO

class VibrateActionType(context: Context) : BaseActionType(context) {

    override val type = TYPE

    override fun fromDTO(actionDTO: ActionDTO) = VibrateAction(this, actionDTO.data)

    override fun getAlias() = ActionAlias(
        functionName = "vibrate",
        parameters = listOf(VibrateAction.KEY_PATTERN, VibrateAction.KEY_WAIT_FOR_COMPLETION)
    )

    companion object {

        const val TYPE = "vibrate"

    }

}
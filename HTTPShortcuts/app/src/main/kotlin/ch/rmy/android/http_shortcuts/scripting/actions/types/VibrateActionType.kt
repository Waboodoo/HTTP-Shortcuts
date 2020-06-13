package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionDTO

class VibrateActionType : BaseActionType() {

    override val type = TYPE

    override fun fromDTO(actionDTO: ActionDTO) = VibrateAction(actionDTO.data)

    override fun getAlias() = ActionAlias(
        functionName = FUNCTION_NAME,
        parameters = listOf(VibrateAction.KEY_PATTERN, VibrateAction.KEY_WAIT_FOR_COMPLETION)
    )

    companion object {

        const val TYPE = "vibrate"
        const val FUNCTION_NAME = "vibrate"

    }

}
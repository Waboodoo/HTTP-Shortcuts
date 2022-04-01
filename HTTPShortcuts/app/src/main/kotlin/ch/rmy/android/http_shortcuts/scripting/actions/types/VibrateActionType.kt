package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionDTO

class VibrateActionType : BaseActionType() {

    override val type = TYPE

    override fun fromDTO(actionDTO: ActionDTO) = VibrateAction(
        patternId = actionDTO.getInt(0) ?: 0,
        waitForCompletion = actionDTO.getBoolean(1) ?: false,
    )

    override fun getAlias() = ActionAlias(
        functionName = FUNCTION_NAME,
        parameters = 2,
    )

    companion object {
        private const val TYPE = "vibrate"
        private const val FUNCTION_NAME = "vibrate"
    }
}

package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionDTO

class VibrateActionType : BaseActionType() {

    override val type = TYPE

    override fun fromDTO(actionDTO: ActionDTO) = VibrateAction(
        patternId = actionDTO[KEY_PATTERN]?.toIntOrNull() ?: 0,
        waitForCompletion = actionDTO[KEY_WAIT_FOR_COMPLETION]?.toBoolean() ?: false,
    )

    override fun getAlias() = ActionAlias(
        functionName = FUNCTION_NAME,
        parameters = listOf(KEY_PATTERN, KEY_WAIT_FOR_COMPLETION),
    )

    companion object {

        const val TYPE = "vibrate"
        const val FUNCTION_NAME = "vibrate"

        const val KEY_PATTERN = "pattern"
        const val KEY_WAIT_FOR_COMPLETION = "wait"

    }

}
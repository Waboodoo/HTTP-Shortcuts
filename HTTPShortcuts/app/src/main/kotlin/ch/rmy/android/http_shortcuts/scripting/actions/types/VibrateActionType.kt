package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionData
import ch.rmy.android.http_shortcuts.scripting.actions.ActionRunnable
import javax.inject.Inject

class VibrateActionType
@Inject
constructor(
    private val vibrateAction: VibrateAction,
) : ActionType {
    override val type = TYPE

    override fun getActionRunnable(actionDTO: ActionData) =
        ActionRunnable(
            action = vibrateAction,
            params = VibrateAction.Params(
                patternId = actionDTO.getInt(0) ?: 0,
                waitForCompletion = actionDTO.getBoolean(1) ?: false,
            ),
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

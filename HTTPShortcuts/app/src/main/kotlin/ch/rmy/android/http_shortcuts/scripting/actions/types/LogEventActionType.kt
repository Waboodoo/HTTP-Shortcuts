package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionData
import ch.rmy.android.http_shortcuts.scripting.actions.ActionRunnable
import javax.inject.Inject

class LogEventActionType
@Inject
constructor(
    private val logEventAction: LogEventAction,
) : ActionType {
    override val type = TYPE

    override fun getActionRunnable(actionDTO: ActionData) =
        ActionRunnable(
            action = logEventAction,
            params = LogEventAction.Params(
                title = actionDTO.getString(0) ?: "",
                message = actionDTO.getString(1),
            ),
        )

    override fun getAlias() = ActionAlias(
        functionName = FUNCTION_NAME,
        parameters = 2,
    )

    companion object {
        private const val TYPE = "log_event"
        private const val FUNCTION_NAME = "logEvent"
    }
}

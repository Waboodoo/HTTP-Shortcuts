package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionRunnable
import ch.rmy.android.scripting.JsFunctionArgs
import javax.inject.Inject

class TriggerTaskerTaskActionType
@Inject
constructor(
    private val triggerTaskerTaskAction: TriggerTaskerTaskAction,
) : ActionType {
    override val type = TYPE

    override fun getActionRunnable(args: JsFunctionArgs) =
        ActionRunnable(
            action = triggerTaskerTaskAction,
            params = TriggerTaskerTaskAction.Params(
                taskName = args.getString(0) ?: "",
                variableValuesJson = args.getString(1) ?: "{}",
            ),
        )

    override fun getAlias() = ActionAlias(
        functionName = FUNCTION_NAME,
        parameters = 2,
    )

    companion object {

        private const val TYPE = "trigger_tasker_task"
        private const val FUNCTION_NAME = "triggerTaskerTask"
    }
}

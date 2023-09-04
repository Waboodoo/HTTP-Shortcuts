package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionData
import ch.rmy.android.http_shortcuts.scripting.actions.ActionRunnable
import javax.inject.Inject

class TriggerTaskerTaskActionType
@Inject
constructor(
    private val triggerTaskerTaskAction: TriggerTaskerTaskAction,
) : ActionType {
    override val type = TYPE

    override fun getActionRunnable(actionDTO: ActionData) =
        ActionRunnable(
            action = triggerTaskerTaskAction,
            params = TriggerTaskerTaskAction.Params(
                taskName = actionDTO.getString(0) ?: "",
                variableValuesJson = actionDTO.getString(1) ?: "{}",
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

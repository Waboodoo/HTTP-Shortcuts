package ch.rmy.android.http_shortcuts.scripting.actions.types

import android.content.Context
import ch.rmy.android.http_shortcuts.plugin.TaskerIntent
import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionDTO

class TriggerTaskerTaskActionType : BaseActionType() {

    override val type = TYPE

    override fun fromDTO(actionDTO: ActionDTO) = TriggerTaskerTaskAction(
        taskName = actionDTO.getString(0) ?: "",
        variableValuesJson = actionDTO.getString(1) ?: "{}",
    )

    override fun getAlias() = ActionAlias(
        functionName = FUNCTION_NAME,
        parameters = 2,
    )

    companion object {

        private const val TYPE = "trigger_tasker_task"
        private const val FUNCTION_NAME = "triggerTaskerTask"

        fun isTaskerAvailable(context: Context): Boolean =
            TaskerIntent.isTaskerInstalled(context)
    }
}

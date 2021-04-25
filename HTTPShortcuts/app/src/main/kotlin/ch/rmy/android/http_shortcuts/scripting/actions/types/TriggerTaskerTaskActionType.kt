package ch.rmy.android.http_shortcuts.scripting.actions.types

import android.content.Context
import ch.rmy.android.http_shortcuts.plugin.TaskerIntent
import ch.rmy.android.http_shortcuts.scripting.ActionAlias
import ch.rmy.android.http_shortcuts.scripting.actions.ActionDTO

class TriggerTaskerTaskActionType : BaseActionType() {

    override val type = TYPE

    override fun fromDTO(actionDTO: ActionDTO) = TriggerTaskerTaskAction(
        taskName = actionDTO[KEY_TASK_NAME] ?: "",
        variableValuesJson = actionDTO[KEY_VARIABLE_VALUES] ?: "{}",
    )

    override fun getAlias() = ActionAlias(
        functionName = FUNCTION_NAME,
        parameters = listOf(
            KEY_TASK_NAME,
            KEY_VARIABLE_VALUES,
        )
    )

    companion object {

        const val TYPE = "trigger_tasker_task"
        const val FUNCTION_NAME = "triggerTaskerTask"

        const val KEY_TASK_NAME = "taskName"
        const val KEY_VARIABLE_VALUES = "variables"

        fun isTaskerAvailable(context: Context): Boolean =
            TaskerIntent.isTaskerInstalled(context)

    }

}

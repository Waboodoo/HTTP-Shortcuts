package ch.rmy.android.http_shortcuts.plugin

import android.content.Context
import ch.rmy.android.http_shortcuts.activities.ExecuteActivity
import ch.rmy.android.http_shortcuts.plugin.VariableHelper.extractVariableMap
import com.joaomgcd.taskerpluginlibrary.action.TaskerPluginRunnerActionNoOutput
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResult
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultSucess

class TriggerShortcutActionRunner : TaskerPluginRunnerActionNoOutput<Input>() {
    override fun run(context: Context, input: TaskerInput<Input>): TaskerPluginResult<Unit> {
        val shortcutId = input.regular.shortcutId
        val variableValues = extractVariableMap(input)
        ExecuteActivity.IntentBuilder(context, shortcutId)
            .variableValues(variableValues)
            .startActivity(context)
        return TaskerPluginResultSucess()
    }
}

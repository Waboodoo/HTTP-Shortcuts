package ch.rmy.android.http_shortcuts.plugin

import android.content.Context
import ch.rmy.android.http_shortcuts.activities.ExecuteActivity
import ch.rmy.android.http_shortcuts.plugin.VariableHelper.extractVariableMap
import com.joaomgcd.taskerpluginlibrary.action.TaskerPluginRunnerActionNoOutput
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResult
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultError
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultSucess
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultUnknown
import java.util.concurrent.TimeoutException

class TriggerShortcutActionRunner : TaskerPluginRunnerActionNoOutput<Input>() {
    override fun run(context: Context, input: TaskerInput<Input>): TaskerPluginResult<Unit> {
        SessionMonitor.onSessionScheduled()
        val shortcutId = input.regular.shortcutId
        val variableValues = extractVariableMap(input)
        ExecuteActivity.IntentBuilder(shortcutId)
            .variableValues(variableValues)
            .startActivity(context)

        return try {
            // TODO: This is a nasty hack, I'm sorry. Let's say this is an experiment for now...
            // I hope to find a better way to monitor whether the request is still in progress
            SessionMonitor.monitorSession(START_TIMEOUT, COMPLETE_TIMEOUT)
            TaskerPluginResultSucess()
        } catch (e: TimeoutException) {
            TaskerPluginResultUnknown()
        } catch (e: SessionMonitor.SessionStartException) {
            TaskerPluginResultError(
                0,
                "Failed to trigger shortcut. Check the app's Troubleshooting section " +
                    "in the Settings for options to mitigate this.",
            )
        }
    }

    companion object {
        private const val START_TIMEOUT = 3000
        private const val COMPLETE_TIMEOUT = 40000
    }
}

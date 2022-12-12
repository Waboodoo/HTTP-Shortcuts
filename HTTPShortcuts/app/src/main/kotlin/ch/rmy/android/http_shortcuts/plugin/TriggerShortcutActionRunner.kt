package ch.rmy.android.http_shortcuts.plugin

import android.content.Context
import ch.rmy.android.framework.extensions.startActivity
import ch.rmy.android.http_shortcuts.activities.ExecuteActivity
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.data.enums.ShortcutTriggerType
import ch.rmy.android.http_shortcuts.plugin.VariableHelper.extractVariableMap
import com.joaomgcd.taskerpluginlibrary.action.TaskerPluginRunnerActionNoOutput
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResult
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultError
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultSucess
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

class TriggerShortcutActionRunner : TaskerPluginRunnerActionNoOutput<Input>() {

    @Inject
    lateinit var sessionMonitor: SessionMonitor

    override fun run(context: Context, input: TaskerInput<Input>): TaskerPluginResult<Unit> {
        context.getApplicationComponent().inject(this)

        sessionMonitor.onSessionScheduled()
        val shortcutId = input.regular.shortcutId
        val variableValues = extractVariableMap(input)
        ExecuteActivity.IntentBuilder(shortcutId)
            .variableValues(variableValues)
            .trigger(ShortcutTriggerType.PLUGIN)
            .startActivity(context)

        return try {
            runBlocking {
                sessionMonitor.monitorSession(START_TIMEOUT, COMPLETE_TIMEOUT)
            }
            TaskerPluginResultSucess()
        } catch (e: TimeoutCancellationException) {
            TaskerPluginResultError(
                0,
                "Failed to trigger shortcut. Check HTTP Shortcuts' Troubleshooting section " +
                    "in the Settings for options to mitigate this.",
            )
        }
    }

    companion object {
        private val START_TIMEOUT = 3.seconds
        private val COMPLETE_TIMEOUT = 40.seconds
    }
}

package ch.rmy.android.http_shortcuts.plugin

import android.content.Context
import ch.rmy.android.http_shortcuts.activities.execute.ExecutionStarter
import ch.rmy.android.http_shortcuts.data.enums.ShortcutTriggerType
import ch.rmy.android.http_shortcuts.plugin.VariableHelper.extractVariableMap
import com.joaomgcd.taskerpluginlibrary.action.TaskerPluginRunnerAction
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResult
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultErrorWithOutput
import com.joaomgcd.taskerpluginlibrary.runner.TaskerPluginResultSucess
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.runBlocking

class TriggerShortcutActionRunner : TaskerPluginRunnerAction<Input, Output>() {

    override fun run(context: Context, input: TaskerInput<Input>): TaskerPluginResult<Output> {
        val entryPoint = EntryPointAccessors.fromApplication<TriggerShortcutActionRunnerEntryPoint>(context)
        val sessionMonitor = entryPoint.sessionMonitor()
        val executionStarter = entryPoint.executionStarter()

        sessionMonitor.onSessionScheduled()
        val shortcutId = input.regular.shortcutId
        val variableValues = extractVariableMap(input)

        executionStarter.execute(
            shortcutId = shortcutId,
            variableValues = variableValues,
            trigger = ShortcutTriggerType.PLUGIN,
        )

        return try {
            val result = runBlocking {
                sessionMonitor.monitorSession(START_TIMEOUT, COMPLETE_TIMEOUT)
            }
            TaskerPluginResultSucess(Output(result))
        } catch (e: TimeoutCancellationException) {
            TaskerPluginResultErrorWithOutput(
                code = 0,
                message = "Failed to trigger shortcut. Check HTTP Shortcuts' Troubleshooting section " +
                    "in the Settings for options to mitigate this.",
            )
        }
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface TriggerShortcutActionRunnerEntryPoint {
        fun sessionMonitor(): SessionMonitor

        fun executionStarter(): ExecutionStarter
    }

    companion object {
        private val START_TIMEOUT = 3.seconds
        private val COMPLETE_TIMEOUT = 40.seconds
    }
}

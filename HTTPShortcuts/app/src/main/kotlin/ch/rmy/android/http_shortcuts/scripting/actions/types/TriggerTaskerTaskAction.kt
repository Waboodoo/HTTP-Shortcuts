package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.plugin.TaskerIntent
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.http_shortcuts.utils.ActivityProvider
import ch.rmy.android.http_shortcuts.utils.GsonUtil
import javax.inject.Inject

class TriggerTaskerTaskAction
@Inject
constructor(
    private val activityProvider: ActivityProvider,
) : Action<TriggerTaskerTaskAction.Params> {
    override suspend fun Params.execute(executionContext: ExecutionContext) {
        val intent = TaskerIntent(taskName)
        getVariableValues(variableValuesJson)
            .forEach { (variableName, value) ->
                intent.addLocalVariable("%${variableName.lowercase()}", value)
            }
        activityProvider.withActivity { activity ->
            activity.sendBroadcast(intent)
        }
    }

    companion object {
        internal fun getVariableValues(json: String): Map<String, String> =
            try {
                GsonUtil.fromJsonObject<Any?>(json)
                    .mapValues { it.value?.toString() ?: "" }
            } catch (e: Exception) {
                emptyMap()
            }
    }

    data class Params(
        val taskName: String,
        val variableValuesJson: String,
    )
}

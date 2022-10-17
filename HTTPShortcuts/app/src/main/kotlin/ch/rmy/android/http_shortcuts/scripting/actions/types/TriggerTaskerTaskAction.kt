package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.plugin.TaskerIntent
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.http_shortcuts.utils.GsonUtil

class TriggerTaskerTaskAction(
    private val taskName: String,
    private val variableValuesJson: String,
) : BaseAction() {

    override suspend fun execute(executionContext: ExecutionContext) {
        val intent = TaskerIntent(taskName)
        getVariableValues(variableValuesJson)
            .forEach { (variableName, value) ->
                intent.addLocalVariable("%${variableName.lowercase()}", value)
            }
        executionContext.context.sendBroadcast(intent)
    }

    companion object {
        private fun getVariableValues(json: String): Map<String, String> =
            try {
                GsonUtil.fromJsonObject<Any?>(json)
                    .mapValues { it.value?.toString() ?: "" }
            } catch (e: Exception) {
                emptyMap()
            }
    }
}

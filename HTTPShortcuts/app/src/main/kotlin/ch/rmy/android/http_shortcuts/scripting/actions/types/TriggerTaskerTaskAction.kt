package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.plugin.TaskerIntent
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.http_shortcuts.utils.GsonUtil
import io.reactivex.Completable

class TriggerTaskerTaskAction(
    private val taskName: String,
    private val variableValuesJson: String,
) : BaseAction() {

    override fun execute(executionContext: ExecutionContext): Completable =
        Completable.fromAction {
            val intent = TaskerIntent(taskName)
            getVariableValues(variableValuesJson)
                .forEach { (variableName, value) ->
                    intent.addLocalVariable("%${variableName.toLowerCase()}", value)
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
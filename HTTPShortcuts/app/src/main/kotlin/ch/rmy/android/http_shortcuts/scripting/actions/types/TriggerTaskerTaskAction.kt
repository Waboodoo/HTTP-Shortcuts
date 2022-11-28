package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.dagger.ApplicationComponent
import ch.rmy.android.http_shortcuts.plugin.TaskerIntent
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.http_shortcuts.utils.ActivityProvider
import ch.rmy.android.http_shortcuts.utils.GsonUtil
import javax.inject.Inject

class TriggerTaskerTaskAction(
    private val taskName: String,
    private val variableValuesJson: String,
) : BaseAction() {

    @Inject
    lateinit var activityProvider: ActivityProvider

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }

    override suspend fun execute(executionContext: ExecutionContext) {
        val intent = TaskerIntent(taskName)
        getVariableValues(variableValuesJson)
            .forEach { (variableName, value) ->
                intent.addLocalVariable("%${variableName.lowercase()}", value)
            }
        activityProvider.getActivity().sendBroadcast(intent)
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
}

package ch.rmy.android.http_shortcuts.plugin

import ch.rmy.android.http_shortcuts.data.Controller
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput
import com.joaomgcd.taskerpluginlibrary.input.TaskerInputInfo
import com.joaomgcd.taskerpluginlibrary.input.TaskerInputInfos

object VariableHelper {

    fun getTaskerInputInfos() =
        TaskerInputInfos().apply {
            getVariableKeys()
                .forEach { variableKey ->
                    add(TaskerInputInfo(
                        key = variableKey,
                        label = variableKey,
                        description = null,
                        ignoreInStringBlurb = false,
                        value = "%$variableKey",
                    ))
                }
        }

    private fun getVariableKeys() =
        Controller().use { controller ->
            controller.getVariables().map { it.key }
        }

    fun extractVariableMap(input: TaskerInput<Input>) =
        input.dynamic.filterIsInstance<TaskerInputInfo>()
            .filter { !it.key.startsWith(TASKER_INPUT_PREFIX) && !it.key.startsWith(Input.FIELD_PREFIX) }
            .filter { it.value is String && it.value != "%${it.key}" }
            .associate { it.key to (it.value as String) }

    private const val TASKER_INPUT_PREFIX = "net.dinglisch.android.tasker.extras."

}

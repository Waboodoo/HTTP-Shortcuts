package ch.rmy.android.http_shortcuts.plugin

import com.joaomgcd.taskerpluginlibrary.input.TaskerInput
import com.joaomgcd.taskerpluginlibrary.input.TaskerInputInfo

object VariableHelper {

    fun extractVariableMap(input: TaskerInput<Input>) =
        input.dynamic.filterIsInstance<TaskerInputInfo>()
            .filter { !it.key.startsWith(TASKER_INPUT_PREFIX) && !it.key.startsWith(Input.FIELD_PREFIX) }
            .filter { it.value is String && it.value != "%${it.key}" }
            .associate { it.key to (it.value as String) }

    private const val TASKER_INPUT_PREFIX = "net.dinglisch.android.tasker.extras."
}

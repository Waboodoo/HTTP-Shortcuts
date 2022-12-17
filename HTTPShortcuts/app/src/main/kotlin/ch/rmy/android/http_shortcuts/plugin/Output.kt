package ch.rmy.android.http_shortcuts.plugin

import ch.rmy.android.http_shortcuts.R
import com.joaomgcd.taskerpluginlibrary.output.TaskerOutputObject
import com.joaomgcd.taskerpluginlibrary.output.TaskerOutputVariable

@TaskerOutputObject
class Output(
    @get:TaskerOutputVariable("result", R.string.label_plugin_result, R.string.label_plugin_result_detail)
    val result: String = "",
)

package ch.rmy.android.http_shortcuts.plugin

import com.joaomgcd.taskerpluginlibrary.output.TaskerOutputObject
import com.joaomgcd.taskerpluginlibrary.output.TaskerOutputVariable

@TaskerOutputObject
class Output(
    @get:TaskerOutputVariable("result", labelResIdName = "label_plugin_result", htmlLabelResIdName = "label_plugin_result_detail")
    val result: String = "",
)

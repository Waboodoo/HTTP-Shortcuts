package ch.rmy.android.http_shortcuts.plugin

import ch.rmy.android.http_shortcuts.R
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfig
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfigHelper
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput

class TriggerShortcutActionHelper(config: TaskerPluginConfig<Input>) : TaskerPluginConfigHelper<Input, Output, TriggerShortcutActionRunner>(config) {

    override val inputClass: Class<Input>
        get() = Input::class.java
    override val outputClass: Class<Output>
        get() = Output::class.java

    override val runnerClass: Class<TriggerShortcutActionRunner>
        get() = TriggerShortcutActionRunner::class.java

    override fun addToStringBlurb(input: TaskerInput<Input>, blurbBuilder: StringBuilder) {
        blurbBuilder.clear()
        blurbBuilder.append(
            context.getString(
                R.string.plugin_blurb_execute_task,
                input.regular.shortcutName,
            ),
        )
    }
}

package ch.rmy.android.http_shortcuts.plugin

import android.os.Bundle
import androidx.activity.result.launch
import ch.rmy.android.framework.ui.Entrypoint
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.activities.main.MainActivity
import ch.rmy.android.http_shortcuts.dagger.ApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableRepository
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfig
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput
import com.joaomgcd.taskerpluginlibrary.input.TaskerInputInfo
import com.joaomgcd.taskerpluginlibrary.input.TaskerInputInfos
import javax.inject.Inject

class PluginEditActivity : BaseActivity(), TaskerPluginConfig<Input>, Entrypoint {

    @Inject
    lateinit var variableRepository: VariableRepository

    private val selectShortcut = registerForActivityResult(MainActivity.SelectShortcut) { result ->
        if (result != null) {
            input = Input(
                shortcutId = result.shortcutId,
                shortcutName = result.shortcutName,
            )
        }
        TriggerShortcutActionHelper(this)
            .finishForTasker()
    }

    private var input: Input? = null

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }

    override fun onCreated(savedState: Bundle?) {
        selectShortcut.launch()
    }

    override val inputForTasker: TaskerInput<Input>
        get() = TaskerInput(
            input ?: Input(
                shortcutId = "???",
                shortcutName = "???",
            ),
            getTaskerInputInfos(),
        )

    private fun getTaskerInputInfos() =
        TaskerInputInfos().apply {
            getVariableKeys()
                .forEach { variableKey ->
                    add(
                        TaskerInputInfo(
                            key = variableKey,
                            label = variableKey,
                            description = null,
                            ignoreInStringBlurb = false,
                            value = "%$variableKey",
                        )
                    )
                }
        }

    private fun getVariableKeys() =
        variableRepository.getVariables()
            .blockingGet() // TODO: Find a way to avoid using blockingGet
            .map { it.key }

    override fun assignFromInput(input: TaskerInput<Input>) {
        this.input = input.regular
    }
}

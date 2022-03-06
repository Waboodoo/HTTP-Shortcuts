package ch.rmy.android.http_shortcuts.plugin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import ch.rmy.android.framework.extensions.startActivity
import ch.rmy.android.framework.ui.Entrypoint
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.activities.main.MainActivity
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableRepository
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfig
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput
import com.joaomgcd.taskerpluginlibrary.input.TaskerInputInfo
import com.joaomgcd.taskerpluginlibrary.input.TaskerInputInfos

class PluginEditActivity : BaseActivity(), TaskerPluginConfig<Input>, Entrypoint {

    private var input: Input? = null

    private val variableRepository = VariableRepository()

    override fun onCreated(savedState: Bundle?) {
        Intent(this, MainActivity::class.java)
            .setAction(ACTION_SELECT_SHORTCUT_FOR_PLUGIN)
            .startActivity(this, REQUEST_SELECT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (requestCode == REQUEST_SELECT) {
            if (resultCode == Activity.RESULT_OK) {
                intent?.extras?.let { extras ->
                    val shortcutId = extras.getString(MainActivity.EXTRA_SELECTION_ID)!!
                    val shortcutName = extras.getString(MainActivity.EXTRA_SELECTION_NAME)!!

                    input = Input(
                        shortcutId = shortcutId,
                        shortcutName = shortcutName,
                    )
                }
            }
            TriggerShortcutActionHelper(this)
                .finishForTasker()
        }
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

    companion object {

        const val ACTION_SELECT_SHORTCUT_FOR_PLUGIN = "ch.rmy.android.http_shortcuts.plugin"
        private const val REQUEST_SELECT = 1
    }
}

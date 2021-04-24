package ch.rmy.android.http_shortcuts.plugin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.activities.main.MainActivity
import ch.rmy.android.http_shortcuts.data.RealmFactory
import ch.rmy.android.http_shortcuts.extensions.startActivity
import ch.rmy.android.http_shortcuts.plugin.VariableHelper.getTaskerInputInfos
import com.joaomgcd.taskerpluginlibrary.config.TaskerPluginConfig
import com.joaomgcd.taskerpluginlibrary.input.TaskerInput

class PluginEditActivity : BaseActivity(), TaskerPluginConfig<Input> {

    private lateinit var input: Input

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RealmFactory.init(applicationContext)

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
        get() = TaskerInput(input, getTaskerInputInfos())

    override fun assignFromInput(input: TaskerInput<Input>) {
        this.input = input.regular
    }

    companion object {

        const val ACTION_SELECT_SHORTCUT_FOR_PLUGIN = "ch.rmy.android.http_shortcuts.plugin"
        private const val REQUEST_SELECT = 1

    }

}

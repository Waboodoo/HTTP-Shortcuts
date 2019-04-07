package ch.rmy.android.http_shortcuts.plugin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.main.MainActivity
import ch.rmy.android.http_shortcuts.data.RealmFactory
import ch.rmy.android.http_shortcuts.extensions.startActivity
import com.twofortyfouram.locale.sdk.client.ui.activity.AbstractFragmentPluginActivity

class PluginEditActivity : AbstractFragmentPluginActivity() {

    private var bundle: Bundle? = null
    private var name: String? = null

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
                    val shortcutId = extras.getString(MainActivity.EXTRA_SELECTION_ID) ?: ""
                    val supportsVariables = TaskerPlugin.Setting.hostSupportsOnFireVariableReplacement(this)
                    bundle = PluginBundleManager.generateBundle(shortcutId, supportsVariables)
                    name = extras.getString(MainActivity.EXTRA_SELECTION_NAME)
                }
            }
            finish()
        }
    }

    override fun isBundleValid(bundle: Bundle) = PluginBundleManager.isBundleValid(bundle)

    override fun onPostCreateWithPreviousResult(bundle: Bundle, s: String) {

    }

    override fun getResultBundle() = bundle

    override fun getResultBlurb(bundle: Bundle): String = getString(R.string.plugin_blurb_execute_task, name)

    companion object {

        const val ACTION_SELECT_SHORTCUT_FOR_PLUGIN = "ch.rmy.android.http_shortcuts.plugin"
        private const val REQUEST_SELECT = 1

    }

}

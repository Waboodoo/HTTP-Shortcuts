package ch.rmy.android.http_shortcuts.plugin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import ch.rmy.android.http_shortcuts.activities.MainActivity
import com.twofortyfouram.locale.sdk.client.ui.activity.AbstractFragmentPluginActivity

class PluginEditActivity : AbstractFragmentPluginActivity() {

    private var bundle: Bundle? = null
    private var name: String? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = Intent(this, MainActivity::class.java)
        intent.action = ACTION_SELECT_SHORTCUT_FOR_PLUGIN

        startActivityForResult(intent, REQUEST_SELECT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (requestCode == REQUEST_SELECT) {
            if (resultCode == Activity.RESULT_OK && intent != null) {
                val id = intent.extras.getLong(MainActivity.EXTRA_SELECTION_ID)
                bundle = PluginBundleManager.generateBundle(id)
                name = intent.extras.getString(MainActivity.EXTRA_SELECTION_NAME)
            }
            finish()
        }
    }

    override fun isBundleValid(bundle: Bundle) = PluginBundleManager.isBundleValid(bundle)

    override fun onPostCreateWithPreviousResult(bundle: Bundle, s: String) {

    }

    override fun getResultBundle() = bundle

    override fun getResultBlurb(bundle: Bundle) = name!!

    companion object {

        const val ACTION_SELECT_SHORTCUT_FOR_PLUGIN = "ch.rmy.android.http_shortcuts.plugin"
        private const val REQUEST_SELECT = 1

    }

}

package ch.rmy.android.http_shortcuts.activities

import android.app.SearchManager
import android.os.Bundle
import android.widget.Toast
import ch.rmy.android.http_shortcuts.realm.Controller
import ch.rmy.android.http_shortcuts.utils.IntentUtil

// THIS IMPLEMENTATION IS EXPERIMENTAL ONLY
class VoiceActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val shortcutName = intent.getStringExtra(SearchManager.QUERY) ?: return

        val controller = destroyer.own(Controller())
        val shortcut = controller.getShortcutByName(shortcutName)
        if (shortcut == null) {
            Toast.makeText(context, "Shortcut \"$shortcutName\" not found", Toast.LENGTH_LONG).show()
            finishWithoutAnimation()
            return
        }

        val intent = IntentUtil.createIntent(context, shortcut.id)
        startActivity(intent)
        finishWithoutAnimation()
    }

}

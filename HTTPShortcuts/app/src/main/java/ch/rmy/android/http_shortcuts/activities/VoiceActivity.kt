package ch.rmy.android.http_shortcuts.activities

import android.app.SearchManager
import android.os.Bundle
import ch.rmy.android.http_shortcuts.realm.Controller
import ch.rmy.android.http_shortcuts.utils.showToast

// THIS IMPLEMENTATION IS EXPERIMENTAL ONLY
class VoiceActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val shortcutName = intent.getStringExtra(SearchManager.QUERY) ?: return

        val controller = destroyer.own(Controller())
        val shortcut = controller.getShortcutByName(shortcutName)
        if (shortcut == null) {
            showToast("Shortcut \"$shortcutName\" not found")
            finishWithoutAnimation()
            return
        }

        val intent = ExecuteActivity.IntentBuilder(context, shortcut.id)
                .build()
        startActivity(intent)
        finishWithoutAnimation()
    }

}

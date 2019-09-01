package ch.rmy.android.http_shortcuts.activities.misc

import android.app.SearchManager
import android.os.Bundle
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.activities.ExecuteActivity
import ch.rmy.android.http_shortcuts.data.DataSource
import ch.rmy.android.http_shortcuts.extensions.showToast
import ch.rmy.android.http_shortcuts.extensions.startActivity

// THIS IMPLEMENTATION IS EXPERIMENTAL ONLY
class VoiceActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val shortcutName = intent.getStringExtra(SearchManager.QUERY) ?: return

        val shortcut = DataSource.getShortcutByNameOrId(shortcutName) ?: run {
            showToast("Shortcut \"$shortcutName\" not found")
            finishWithoutAnimation()
            return
        }

        ExecuteActivity.IntentBuilder(context, shortcut.id)
            .build()
            .startActivity(this)
        finishWithoutAnimation()
    }

}

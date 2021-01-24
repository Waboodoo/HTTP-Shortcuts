package ch.rmy.android.http_shortcuts.activities.misc

import android.app.SearchManager
import android.os.Bundle
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.activities.Entrypoint
import ch.rmy.android.http_shortcuts.activities.ExecuteActivity
import ch.rmy.android.http_shortcuts.data.DataSource
import ch.rmy.android.http_shortcuts.extensions.finishWithoutAnimation
import ch.rmy.android.http_shortcuts.extensions.showToast

class VoiceActivity : BaseActivity(), Entrypoint {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!isRealmAvailable) {
            return
        }

        val shortcutName = intent.getStringExtra(SearchManager.QUERY) ?: return

        val shortcut = DataSource.getShortcutByNameOrId(shortcutName) ?: run {
            showToast(getString(R.string.error_shortcut_not_found_for_deep_link, shortcutName), long = true)
            finishWithoutAnimation()
            return
        }

        ExecuteActivity.IntentBuilder(context, shortcut.id)
            .startActivity(this)
        finishWithoutAnimation()
    }

}

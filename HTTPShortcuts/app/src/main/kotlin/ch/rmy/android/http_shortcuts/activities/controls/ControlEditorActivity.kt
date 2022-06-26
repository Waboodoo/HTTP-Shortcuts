package ch.rmy.android.http_shortcuts.activities.controls

import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId

class ControlEditorActivity : BaseActivity() {

    // TODO

    class IntentBuilder(shortcutId: ShortcutId) : BaseIntentBuilder(ControlEditorActivity::class) {
        init {
            intent.putExtra(EXTRA_SHORTCUT_ID, shortcutId)
        }
    }

    companion object {
        private const val EXTRA_SHORTCUT_ID = "shortcut_id"
    }
}

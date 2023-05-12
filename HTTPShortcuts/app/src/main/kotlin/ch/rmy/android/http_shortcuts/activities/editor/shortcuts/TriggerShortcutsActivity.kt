package ch.rmy.android.http_shortcuts.activities.editor.shortcuts

import androidx.compose.runtime.Composable
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.activities.BaseComposeActivity
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId

class TriggerShortcutsActivity : BaseComposeActivity() {

    @Composable
    override fun Content() {
        TriggerShortcutsScreen(currentShortcutId = intent.getStringExtra(EXTRA_SHORTCUT_ID))
    }

    class IntentBuilder : BaseIntentBuilder(TriggerShortcutsActivity::class) {

        fun shortcutId(shortcutId: ShortcutId?) = also {
            intent.putExtra(EXTRA_SHORTCUT_ID, shortcutId)
        }
    }

    companion object {
        private const val EXTRA_SHORTCUT_ID = "shortcutId"
    }
}

package ch.rmy.android.http_shortcuts.activities.editor.scripting

import androidx.compose.runtime.Composable
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.activities.BaseComposeActivity
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ScriptingActivity : BaseComposeActivity() {

    @Composable
    override fun Content() {
        ScriptingScreen(intent.getStringExtra(EXTRA_SHORTCUT_ID))
    }

    class IntentBuilder : BaseIntentBuilder(ScriptingActivity::class) {

        fun shortcutId(shortcutId: ShortcutId?) = also {
            intent.putExtra(EXTRA_SHORTCUT_ID, shortcutId)
        }
    }

    companion object {
        private const val EXTRA_SHORTCUT_ID = "shortcutId"
    }
}

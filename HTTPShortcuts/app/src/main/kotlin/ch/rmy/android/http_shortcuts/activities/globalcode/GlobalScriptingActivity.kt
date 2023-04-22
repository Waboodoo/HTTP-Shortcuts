package ch.rmy.android.http_shortcuts.activities.globalcode

import androidx.compose.runtime.Composable
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.activities.BaseComposeActivity
import ch.rmy.android.http_shortcuts.components.ScreenScope

class GlobalScriptingActivity : BaseComposeActivity() {

    @Composable
    override fun ScreenScope.Content() {
        GlobalScriptingScreen()
    }

    class IntentBuilder : BaseIntentBuilder(GlobalScriptingActivity::class)
}

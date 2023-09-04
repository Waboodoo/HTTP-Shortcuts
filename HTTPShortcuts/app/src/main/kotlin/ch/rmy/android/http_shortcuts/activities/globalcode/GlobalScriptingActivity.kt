package ch.rmy.android.http_shortcuts.activities.globalcode

import androidx.compose.runtime.Composable
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.activities.BaseComposeActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GlobalScriptingActivity : BaseComposeActivity() {

    @Composable
    override fun Content() {
        GlobalScriptingScreen()
    }

    class IntentBuilder : BaseIntentBuilder(GlobalScriptingActivity::class)
}

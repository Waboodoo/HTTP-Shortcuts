package ch.rmy.android.http_shortcuts.activities.about

import androidx.compose.runtime.Composable
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.activities.BaseComposeActivity

class AboutActivity : BaseComposeActivity() {

    @Composable
    override fun Content() {
        AboutScreen()
    }

    class IntentBuilder : BaseIntentBuilder(AboutActivity::class)
}

package ch.rmy.android.http_shortcuts.activities.settings.about

import androidx.compose.runtime.Composable
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.activities.BaseComposeActivity
import ch.rmy.android.http_shortcuts.components.ScreenScope

class AboutActivity : BaseComposeActivity() {

    @Composable
    override fun ScreenScope.Content() {
        AboutScreen()
    }

    class IntentBuilder : BaseIntentBuilder(AboutActivity::class)
}

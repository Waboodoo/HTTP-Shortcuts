package ch.rmy.android.http_shortcuts.activities.settings.acknowledgment

import androidx.compose.runtime.Composable
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.activities.BaseComposeActivity
import ch.rmy.android.http_shortcuts.components.ScreenScope

class AcknowledgmentActivity : BaseComposeActivity() {

    @Composable
    override fun ScreenScope.Content() {
        AcknowledgmentScreen()
    }

    class IntentBuilder : BaseIntentBuilder(AcknowledgmentActivity::class)
}

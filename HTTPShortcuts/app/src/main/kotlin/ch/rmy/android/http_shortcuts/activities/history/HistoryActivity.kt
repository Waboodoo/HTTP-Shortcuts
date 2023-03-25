package ch.rmy.android.http_shortcuts.activities.history

import androidx.compose.runtime.Composable
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.activities.BaseComposeActivity
import ch.rmy.android.http_shortcuts.components.ScreenScope

class HistoryActivity : BaseComposeActivity() {

    @Composable
    override fun ScreenScope.Content() {
        HistoryScreen()
    }

    class IntentBuilder : BaseIntentBuilder(HistoryActivity::class)
}

package ch.rmy.android.http_shortcuts.activities.history

import androidx.compose.runtime.Composable
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.activities.BaseComposeActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HistoryActivity : BaseComposeActivity() {

    @Composable
    override fun Content() {
        HistoryScreen()
    }

    class IntentBuilder : BaseIntentBuilder(HistoryActivity::class)
}

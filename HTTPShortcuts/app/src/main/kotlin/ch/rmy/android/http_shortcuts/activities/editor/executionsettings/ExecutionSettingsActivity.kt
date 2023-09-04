package ch.rmy.android.http_shortcuts.activities.editor.executionsettings

import androidx.compose.runtime.Composable
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.activities.BaseComposeActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ExecutionSettingsActivity : BaseComposeActivity() {

    @Composable
    override fun Content() {
        ExecutionSettingsScreen()
    }

    class IntentBuilder : BaseIntentBuilder(ExecutionSettingsActivity::class)
}

package ch.rmy.android.http_shortcuts.activities.editor.executionsettings

import androidx.compose.runtime.Composable
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.activities.BaseComposeActivity
import ch.rmy.android.http_shortcuts.components.ScreenScope

class ExecutionSettingsActivity : BaseComposeActivity() {

    @Composable
    override fun ScreenScope.Content() {
        ExecutionSettingsScreen()
    }

    class IntentBuilder : BaseIntentBuilder(ExecutionSettingsActivity::class)
}

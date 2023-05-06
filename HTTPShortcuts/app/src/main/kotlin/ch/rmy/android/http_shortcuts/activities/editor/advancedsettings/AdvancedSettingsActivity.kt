package ch.rmy.android.http_shortcuts.activities.editor.advancedsettings

import androidx.compose.runtime.Composable
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.activities.BaseComposeActivity
import ch.rmy.android.http_shortcuts.components.ScreenScope

class AdvancedSettingsActivity : BaseComposeActivity() {

    @Composable
    override fun ScreenScope.Content() {
        AdvancedSettingsScreen()
    }

    class IntentBuilder : BaseIntentBuilder(AdvancedSettingsActivity::class)
}

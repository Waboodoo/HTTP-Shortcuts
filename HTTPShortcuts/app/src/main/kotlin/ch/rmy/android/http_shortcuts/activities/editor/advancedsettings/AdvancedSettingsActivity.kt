package ch.rmy.android.http_shortcuts.activities.editor.advancedsettings

import androidx.compose.runtime.Composable
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.activities.BaseComposeActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AdvancedSettingsActivity : BaseComposeActivity() {

    @Composable
    override fun Content() {
        AdvancedSettingsScreen()
    }

    class IntentBuilder : BaseIntentBuilder(AdvancedSettingsActivity::class)
}

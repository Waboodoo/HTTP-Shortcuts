package ch.rmy.android.http_shortcuts.activities.editor.basicsettings

import androidx.compose.runtime.Composable
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.activities.BaseComposeActivity
import ch.rmy.android.http_shortcuts.components.ScreenScope

class BasicRequestSettingsActivity : BaseComposeActivity() {

    @Composable
    override fun ScreenScope.Content() {
        BasicRequestSettingsScreen()
    }

    class IntentBuilder : BaseIntentBuilder(BasicRequestSettingsActivity::class)
}

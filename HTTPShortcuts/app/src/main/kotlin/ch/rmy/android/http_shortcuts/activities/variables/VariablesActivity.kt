package ch.rmy.android.http_shortcuts.activities.variables

import androidx.compose.runtime.Composable
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.activities.BaseComposeActivity
import ch.rmy.android.http_shortcuts.components.ScreenScope

class VariablesActivity : BaseComposeActivity() {

    @Composable
    override fun ScreenScope.Content() {
        VariablesScreen()
    }

    class IntentBuilder : BaseIntentBuilder(VariablesActivity::class)
}

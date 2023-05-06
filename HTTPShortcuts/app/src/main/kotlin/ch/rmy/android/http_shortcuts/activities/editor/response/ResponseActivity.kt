package ch.rmy.android.http_shortcuts.activities.editor.response

import androidx.compose.runtime.Composable
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.activities.BaseComposeActivity
import ch.rmy.android.http_shortcuts.components.ScreenScope

class ResponseActivity : BaseComposeActivity() {

    @Composable
    override fun ScreenScope.Content() {
        ResponseScreen()
    }

    class IntentBuilder : BaseIntentBuilder(ResponseActivity::class)
}

package ch.rmy.android.http_shortcuts.activities.editor.body

import androidx.compose.runtime.Composable
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.activities.BaseComposeActivity
import ch.rmy.android.http_shortcuts.components.ScreenScope

class RequestBodyActivity : BaseComposeActivity() {

    @Composable
    override fun ScreenScope.Content() {
        RequestBodyScreen()
    }
    class IntentBuilder : BaseIntentBuilder(RequestBodyActivity::class)
}

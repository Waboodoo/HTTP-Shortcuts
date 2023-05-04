package ch.rmy.android.http_shortcuts.activities.editor.authentication

import androidx.compose.runtime.Composable
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.activities.BaseComposeActivity
import ch.rmy.android.http_shortcuts.components.ScreenScope

class AuthenticationActivity : BaseComposeActivity() {

    @Composable
    override fun ScreenScope.Content() {
        AuthenticationScreen()
    }

    class IntentBuilder : BaseIntentBuilder(AuthenticationActivity::class)
}

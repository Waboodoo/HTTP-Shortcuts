package ch.rmy.android.http_shortcuts.activities.editor.body

import androidx.compose.runtime.Composable
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.activities.BaseComposeActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RequestBodyActivity : BaseComposeActivity() {

    @Composable
    override fun Content() {
        RequestBodyScreen()
    }
    class IntentBuilder : BaseIntentBuilder(RequestBodyActivity::class)
}

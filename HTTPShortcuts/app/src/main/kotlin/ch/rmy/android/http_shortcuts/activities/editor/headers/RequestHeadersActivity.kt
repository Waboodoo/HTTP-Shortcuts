package ch.rmy.android.http_shortcuts.activities.editor.headers

import androidx.compose.runtime.Composable
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.activities.BaseComposeActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RequestHeadersActivity : BaseComposeActivity() {

    @Composable
    override fun Content() {
        RequestHeadersScreen()
    }

    class IntentBuilder : BaseIntentBuilder(RequestHeadersActivity::class)
}

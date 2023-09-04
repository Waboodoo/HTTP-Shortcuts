package ch.rmy.android.http_shortcuts.activities.editor.response

import androidx.compose.runtime.Composable
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.activities.BaseComposeActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ResponseActivity : BaseComposeActivity() {

    @Composable
    override fun Content() {
        ResponseScreen()
    }

    class IntentBuilder : BaseIntentBuilder(ResponseActivity::class)
}

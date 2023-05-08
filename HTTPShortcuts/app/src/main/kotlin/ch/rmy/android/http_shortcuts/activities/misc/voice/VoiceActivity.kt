package ch.rmy.android.http_shortcuts.activities.misc.voice

import android.app.SearchManager
import androidx.compose.runtime.Composable
import ch.rmy.android.framework.ui.Entrypoint
import ch.rmy.android.http_shortcuts.activities.BaseComposeActivity
import ch.rmy.android.http_shortcuts.components.ScreenScope

class VoiceActivity : BaseComposeActivity(), Entrypoint {

    override val initializeWithTheme: Boolean
        get() = false

    @Composable
    override fun ScreenScope.Content() {
        VoiceScreen(
            shortcutName = intent.getStringExtra(SearchManager.QUERY),
        )
    }
}

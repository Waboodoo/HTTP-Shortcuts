package ch.rmy.android.http_shortcuts.activities.misc.deeplink

import androidx.compose.runtime.Composable
import ch.rmy.android.framework.ui.Entrypoint
import ch.rmy.android.http_shortcuts.activities.BaseComposeActivity
import ch.rmy.android.http_shortcuts.components.ScreenScope

class DeepLinkActivity : BaseComposeActivity(), Entrypoint {

    override val initializeWithTheme: Boolean
        get() = false

    @Composable
    override fun ScreenScope.Content() {
        DeepLinkScreen(
            url = intent.data,
        )
    }
}

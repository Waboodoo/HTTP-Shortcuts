package ch.rmy.android.http_shortcuts.activities.misc.deeplink

import android.content.Intent
import androidx.compose.runtime.Composable
import ch.rmy.android.http_shortcuts.activities.BaseComposeActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DeepLinkActivity : BaseComposeActivity() {

    override val initializeWithTheme: Boolean
        get() = false

    @Composable
    override fun Content() {
        DeepLinkScreen(
            url = intent.data,
            title = intent.getStringExtra(Intent.EXTRA_TITLE),
            text = intent.getStringExtra(Intent.EXTRA_TEXT),
        )
    }
}

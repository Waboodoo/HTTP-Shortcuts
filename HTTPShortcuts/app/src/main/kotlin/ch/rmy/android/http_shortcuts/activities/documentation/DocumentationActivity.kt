package ch.rmy.android.http_shortcuts.activities.documentation

import android.net.Uri
import androidx.compose.runtime.Composable
import ch.rmy.android.framework.extensions.getParcelable
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.activities.BaseComposeActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DocumentationActivity : BaseComposeActivity() {

    @Composable
    override fun Content() {
        DocumentationScreen(url = intent.getParcelable(EXTRA_URL))
    }

    class IntentBuilder : BaseIntentBuilder(DocumentationActivity::class) {

        fun url(url: Uri) = also {
            intent.putExtra(EXTRA_URL, url)
        }
    }

    companion object {
        private const val EXTRA_URL = "url"
    }
}

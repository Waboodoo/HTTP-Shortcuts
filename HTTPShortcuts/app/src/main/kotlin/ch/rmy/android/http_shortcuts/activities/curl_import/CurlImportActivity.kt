package ch.rmy.android.http_shortcuts.activities.curl_import

import android.content.Intent
import androidx.compose.runtime.Composable
import ch.rmy.android.framework.extensions.createIntent
import ch.rmy.android.framework.extensions.getSerializable
import ch.rmy.android.framework.ui.BaseActivityResultContract
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.activities.BaseComposeActivity
import ch.rmy.android.http_shortcuts.components.ScreenScope
import ch.rmy.curlcommand.CurlCommand

class CurlImportActivity : BaseComposeActivity() {

    @Composable
    override fun ScreenScope.Content() {
        CurlImportScreen()
    }

    object ImportFromCurl : BaseActivityResultContract<IntentBuilder, CurlCommand?>(CurlImportActivity::IntentBuilder) {

        private const val EXTRA_CURL_COMMAND = "curl_command"

        override fun parseResult(resultCode: Int, intent: Intent?): CurlCommand? =
            intent?.getSerializable(EXTRA_CURL_COMMAND)

        fun createResult(command: CurlCommand): Intent =
            createIntent {
                putExtra(EXTRA_CURL_COMMAND, command)
            }
    }

    class IntentBuilder : BaseIntentBuilder(CurlImportActivity::class)
}

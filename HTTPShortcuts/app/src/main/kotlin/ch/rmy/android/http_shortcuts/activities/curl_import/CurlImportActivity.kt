package ch.rmy.android.http_shortcuts.activities.curl_import

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import ch.rmy.android.framework.extensions.createIntent
import ch.rmy.android.framework.extensions.getSerializable
import ch.rmy.android.framework.ui.BaseActivityResultContract
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.activities.BaseActivity
import ch.rmy.android.http_shortcuts.components.Screen
import ch.rmy.curlcommand.CurlCommand

class CurlImportActivity : BaseActivity() {

    override fun onCreated(savedState: Bundle?) {
        updateStatusBarColor()
        val primaryColor = themeHelper.getPrimaryColor(this)
        setContent {
            Screen(primaryColor, ::handleEvent) {
                CurlImportScreen()
            }
        }
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

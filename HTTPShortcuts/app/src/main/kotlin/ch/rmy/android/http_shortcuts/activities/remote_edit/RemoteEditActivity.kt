package ch.rmy.android.http_shortcuts.activities.remote_edit

import android.content.Intent
import androidx.compose.runtime.Composable
import ch.rmy.android.framework.extensions.createIntent
import ch.rmy.android.framework.ui.BaseActivityResultContract
import ch.rmy.android.framework.ui.BaseIntentBuilder
import ch.rmy.android.http_shortcuts.activities.BaseComposeActivity

class RemoteEditActivity : BaseComposeActivity() {

    @Composable
    override fun Content() {
        RemoteEditScreen()
    }

    object OpenRemoteEditor : BaseActivityResultContract<IntentBuilder, Boolean>(::IntentBuilder) {
        private const val EXTRA_CHANGES_IMPORTED = "changes_imported"

        override fun parseResult(resultCode: Int, intent: Intent?): Boolean =
            intent?.getBooleanExtra(EXTRA_CHANGES_IMPORTED, false) ?: false

        fun createResult(changesImported: Boolean) =
            createIntent {
                putExtra(EXTRA_CHANGES_IMPORTED, changesImported)
            }
    }

    class IntentBuilder : BaseIntentBuilder(RemoteEditActivity::class)
}

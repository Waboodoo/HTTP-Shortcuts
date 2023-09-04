package ch.rmy.android.http_shortcuts.activities.misc.share

import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import ch.rmy.android.framework.extensions.getParcelable
import ch.rmy.android.http_shortcuts.activities.BaseComposeActivity
import ch.rmy.android.http_shortcuts.utils.LauncherShortcutManager.Companion.extractShortcutId
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShareActivity : BaseComposeActivity() {

    override val initializeWithTheme: Boolean
        get() = false

    @Composable
    override fun Content() {
        ShareScreen(
            text = intent.getStringExtra(Intent.EXTRA_TEXT),
            title = intent.getStringExtra(Intent.EXTRA_SUBJECT),
            fileUris = intent.getFileUris(),
            shortcutId = intent.extractShortcutId(),
        )
    }

    private fun Intent.getFileUris(): List<Uri> =
        if (action == Intent.ACTION_SEND) {
            getParcelable<Uri>(Intent.EXTRA_STREAM)?.let { listOf(it) }
        } else {
            getParcelableArrayListExtra(Intent.EXTRA_STREAM)
        }
            ?: emptyList()
}

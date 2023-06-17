package ch.rmy.android.http_shortcuts.activities.misc.share

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.compose.runtime.Composable
import ch.rmy.android.framework.extensions.finishWithoutAnimation
import ch.rmy.android.framework.extensions.getParcelable
import ch.rmy.android.framework.extensions.showToast
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.BaseComposeActivity
import ch.rmy.android.http_shortcuts.data.RealmFactoryImpl

class ShareActivity : BaseComposeActivity() {

    override val initializeWithTheme: Boolean
        get() = false

    override fun onCreated(savedState: Bundle?) {
        super.onCreated(savedState)

        // TODO: Remove this check eventually
        if (RealmFactoryImpl.realmError != null) {
            showToast(R.string.error_generic)
            finishWithoutAnimation()
            return
        }
    }

    @Composable
    override fun Content() {
        ShareScreen(
            text = intent.getStringExtra(Intent.EXTRA_TEXT),
            title = intent.getStringExtra(Intent.EXTRA_SUBJECT),
            fileUris = intent.getFileUris(),
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

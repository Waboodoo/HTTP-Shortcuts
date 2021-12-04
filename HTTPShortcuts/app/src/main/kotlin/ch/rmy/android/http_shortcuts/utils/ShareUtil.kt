package ch.rmy.android.http_shortcuts.utils

import android.content.Context
import android.content.Intent
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.extensions.logException
import ch.rmy.android.http_shortcuts.extensions.showToast
import ch.rmy.android.http_shortcuts.extensions.startActivity

object ShareUtil {

    fun shareText(context: Context, text: String) {
        try {
            Intent(Intent.ACTION_SEND)
                .setType(TYPE_TEXT)
                .putExtra(Intent.EXTRA_TEXT, text)
                .let {
                    Intent.createChooser(it, context.getString(R.string.share_title))
                        .startActivity(context)
                }
        } catch (e: Exception) {
            context.showToast(context.getString(R.string.error_share_failed), long = true)
            logException(e)
        }
    }

    private const val TYPE_TEXT = "text/plain"
}

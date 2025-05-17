package ch.rmy.android.framework.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.TransactionTooLargeException
import androidx.annotation.UiThread
import androidx.core.content.getSystemService
import ch.rmy.android.framework.extensions.truncate
import javax.inject.Inject

class ClipboardUtil
@Inject
constructor(
    private val context: Context,
) {
    @UiThread
    fun copyToClipboard(text: String) {
        try {
            val clip = ClipData.newPlainText(null, text)
            clipboardManager?.setPrimaryClip(clip)
        } catch (_: TransactionTooLargeException) {
            val clip = ClipData.newPlainText(null, text.truncate(MAX_LENGTH))
            clipboardManager?.setPrimaryClip(clip)
        }
    }

    fun getFromClipboard(): CharSequence? =
        clipboardManager
            ?.primaryClip
            ?.takeIf { it.itemCount > 0 }
            ?.getItemAt(0)
            ?.coerceToText(context)

    private val clipboardManager
        get() = context.getSystemService<ClipboardManager>()

    companion object {
        private const val MAX_LENGTH = 800_000
    }
}

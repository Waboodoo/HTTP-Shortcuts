package ch.rmy.android.framework.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.annotation.UiThread
import androidx.core.content.getSystemService
import javax.inject.Inject

class ClipboardUtil
@Inject
constructor(
    private val context: Context,
) {

    @UiThread
    fun copyToClipboard(text: String) {
        val clip = ClipData.newPlainText(null, text)
        clipboardManager?.setPrimaryClip(clip)
    }

    fun getFromClipboard(): CharSequence? =
        clipboardManager
            ?.primaryClip
            ?.takeIf { it.itemCount > 0 }
            ?.getItemAt(0)
            ?.coerceToText(context)

    private val clipboardManager
        get() = context.getSystemService<ClipboardManager>()
}

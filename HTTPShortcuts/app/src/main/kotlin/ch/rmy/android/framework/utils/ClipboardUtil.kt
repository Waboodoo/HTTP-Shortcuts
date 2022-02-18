package ch.rmy.android.framework.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

object ClipboardUtil {

    fun copyToClipboard(context: Context, text: String) {
        val clip = ClipData.newPlainText(null, text)
        getClipboardManager(context).setPrimaryClip(clip)
    }

    private fun getClipboardManager(context: Context) =
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
}

package ch.rmy.android.http_shortcuts.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

object ClipboardUtil {

    fun copyToClipboard(context: Context, text: String) {
        val clip = ClipData.newPlainText(null, text)
        getClipboardManager(context).setPrimaryClip(clip)
    }

    fun copyFromClipboard(context: Context): String? {
        val clipboard = getClipboardManager(context)
        val clip = clipboard.primaryClip ?: return null
        if (clip.itemCount < 1 || !clip.description.hasMimeType("text/plain")) {
            return null
        }
        return clip.getItemAt(0).text?.toString()
    }

    private fun getClipboardManager(context: Context) =
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

}
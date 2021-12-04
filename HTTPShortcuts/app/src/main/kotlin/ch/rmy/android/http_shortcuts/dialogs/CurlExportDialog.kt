package ch.rmy.android.http_shortcuts.dialogs

import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.utils.ClipboardUtil
import ch.rmy.android.http_shortcuts.utils.ShareUtil

class CurlExportDialog(private val context: Context, private val title: String, private val curlCommand: String) {

    fun show() {
        val layoutInflater = LayoutInflater.from(context)
        val view = layoutInflater.inflate(R.layout.curl_export_dialog, null)

        DialogBuilder(context)
            .title(title)
            .view(view)
            .neutral(android.R.string.cancel)
            .negative(R.string.share_button) { shareCurlExport() }
            .positive(R.string.button_copy_curl_export) { copyCurlExport() }
            .showIfPossible()

        view.findViewById<TextView>(R.id.curl_export_textview).text = curlCommand
    }

    private fun shareCurlExport() {
        ShareUtil.shareText(context, curlCommand)
    }

    private fun copyCurlExport() {
        ClipboardUtil.copyToClipboard(context, curlCommand)
    }
}

package ch.rmy.android.http_shortcuts.dialogs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.widget.TextView
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.http.ShortcutResponse
import ch.rmy.android.http_shortcuts.utils.showIfPossible
import com.afollestad.materialdialogs.MaterialDialog

class CurlExportDialog(private val context: Context, private val title: String, private val curlCommand: String) {

    fun show() {
        val layoutInflater = LayoutInflater.from(context)
        val view = layoutInflater.inflate(R.layout.curl_export_dialog, null)

        MaterialDialog.Builder(context)
                .title(title)
                .customView(view, false)
                .neutralText(android.R.string.cancel)
                .negativeText(R.string.share_button)
                .onNegative { _, _ -> shareCurlExport() }
                .positiveText(R.string.button_copy_curl_export)
                .onPositive { _, _ -> copyCurlExport() }
                .showIfPossible()

        view.findViewById<TextView>(R.id.curl_export_textview).text = curlCommand
    }

    private fun shareCurlExport() {
        val sharingIntent = Intent(android.content.Intent.ACTION_SEND)
        sharingIntent.type = ShortcutResponse.TYPE_TEXT
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, curlCommand)
        context.startActivity(Intent.createChooser(sharingIntent, context.getString(R.string.share_title)))
    }

    private fun copyCurlExport() {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(null, curlCommand)
        clipboard.setPrimaryClip(clip)
    }

}

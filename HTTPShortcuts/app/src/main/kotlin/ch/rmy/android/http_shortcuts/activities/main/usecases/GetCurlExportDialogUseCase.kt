package ch.rmy.android.http_shortcuts.activities.main.usecases

import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import ch.rmy.android.framework.utils.ClipboardUtil
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.utils.ShareUtil
import ch.rmy.curlcommand.CurlCommand
import ch.rmy.curlcommand.CurlConstructor
import com.afollestad.materialdialogs.callbacks.onShow

class GetCurlExportDialogUseCase {

    operator fun invoke(title: String, command: CurlCommand): DialogState =
        DialogState.create {
            val curlCommand = CurlConstructor.toCurlCommandString(command)

            val layoutInflater = LayoutInflater.from(context)
            val view = layoutInflater.inflate(R.layout.curl_export_dialog, null)

            this
                .title(title)
                .view(view)
                .neutral(android.R.string.cancel)
                .negative(R.string.share_button) { shareCurlExport(context, curlCommand) }
                .positive(R.string.button_copy_curl_export) { copyCurlExport(context, curlCommand) }
                .build()
                .apply {
                    onShow {
                        view.findViewById<TextView>(R.id.curl_export_textview).text = curlCommand
                    }
                }
        }

    private fun shareCurlExport(context: Context, curlCommand: String) {
        ShareUtil.shareText(context, curlCommand)
    }

    private fun copyCurlExport(context: Context, curlCommand: String) {
        ClipboardUtil.copyToClipboard(context, curlCommand)
    }
}

package ch.rmy.android.http_shortcuts.dialogs

import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.data.models.Shortcut

class ShortcutInfoDialog(
    private val context: Context,
    private val shortcut: Shortcut,
) {

    fun show() {
        val layoutInflater = LayoutInflater.from(context)
        val view = layoutInflater.inflate(R.layout.shortcut_info_dialog, null)

        DialogBuilder(context)
            .title(shortcut.name)
            .view(view)
            .positive(android.R.string.ok)
            .showIfPossible()

        view.findViewById<TextView>(R.id.shortcut_id).text = shortcut.id
        view.findViewById<TextView>(R.id.deep_link_url).text = getDeepLinkUrl()
    }

    private fun getDeepLinkUrl() =
        "http-shortcuts://${shortcut.id}"
}

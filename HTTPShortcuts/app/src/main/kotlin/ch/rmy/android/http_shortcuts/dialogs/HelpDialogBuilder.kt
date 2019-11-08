package ch.rmy.android.http_shortcuts.dialogs

import android.content.Context
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.utils.HTMLUtil

class HelpDialogBuilder(context: Context) : DialogBuilder(context) {

    private val view: View

    init {
        val layoutInflater = LayoutInflater.from(context)
        view = layoutInflater.inflate(R.layout.help_dialog, null)
        view(view)
        positive(android.R.string.ok)
    }

    override fun message(text: CharSequence): DialogBuilder = also {
        val textView = view.findViewById<TextView>(R.id.help_text)
        textView.text = HTMLUtil.getHTML(text.toString())
        textView.movementMethod = LinkMovementMethod.getInstance()
    }

}

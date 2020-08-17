package ch.rmy.android.http_shortcuts.utils

import android.os.Build
import android.text.Html
import android.text.Spanned

object HTMLUtil {

    fun getHTML(string: String): Spanned =
        fromHTML(normalize(string))

    private fun normalize(string: String): String =
        string.replace("<pre>", "<tt>")
            .replace("</pre>", "</tt>")

    @Suppress("DEPRECATION")
    private fun fromHTML(string: String): Spanned =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(string, 0)
        } else {
            Html.fromHtml(string)
        }

    fun format(string: String): Spanned = getHTML(
        string.replace("\n", "<br>")
    )

}

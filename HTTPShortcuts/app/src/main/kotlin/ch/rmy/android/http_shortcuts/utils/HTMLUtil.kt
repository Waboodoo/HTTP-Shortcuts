package ch.rmy.android.http_shortcuts.utils

import android.text.Html
import android.text.Spanned

object HTMLUtil {
    fun toSpanned(string: String): Spanned =
        fromHTML(string.convertNewlines().normalize())

    private fun String.normalize(): String =
        replace("<pre>", "<tt>")
            .replace("</pre>", "</tt>")

    private fun String.convertNewlines() =
        removeSuffix("\n").replace("\n", "<br>")

    private fun fromHTML(string: String): Spanned =
        Html.fromHtml(string, 0, null, null)
}

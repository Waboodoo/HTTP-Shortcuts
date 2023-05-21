package ch.rmy.android.http_shortcuts.utils

import android.os.Build
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

    @Suppress("DEPRECATION")
    private fun fromHTML(string: String): Spanned =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(string, 0, null, null)
        } else {
            Html.fromHtml(string, null, null)
        }
}

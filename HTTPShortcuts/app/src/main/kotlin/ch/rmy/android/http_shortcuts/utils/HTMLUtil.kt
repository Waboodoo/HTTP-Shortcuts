package ch.rmy.android.http_shortcuts.utils

import android.os.Build
import android.text.Html
import android.text.Spanned

object HTMLUtil {

    @Suppress("DEPRECATION")
    fun getHTML(string: String): Spanned =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(string, 0)
        } else {
            Html.fromHtml(string)
        }

    fun format(string: String): Spanned = getHTML(
        string.replace("\n", "<br>")
    )

}

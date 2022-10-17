package ch.rmy.android.http_shortcuts.utils

import android.content.Context
import android.os.Build
import android.text.Html
import android.text.Spanned
import kotlinx.coroutines.CoroutineScope

object HTMLUtil {
    fun format(string: String): Spanned =
        fromHTML(string.convertNewlines().normalize())

    fun formatWithImageSupport(
        string: String,
        context: Context,
        onImageLoaded: () -> Unit,
        coroutineScope: CoroutineScope,
    ): Spanned =
        fromHTML(string.convertNewlines().normalize(), ImageGetter(context, onImageLoaded, coroutineScope))

    private fun String.normalize(): String =
        replace("<pre>", "<tt>")
            .replace("</pre>", "</tt>")

    private fun String.convertNewlines() =
        removeSuffix("\n").replace("\n", "<br>")

    @Suppress("DEPRECATION")
    private fun fromHTML(
        string: String,
        imageGetter: ImageGetter? = null,
    ): Spanned =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(string, 0, imageGetter, null)
        } else {
            Html.fromHtml(string, imageGetter, null)
        }
}

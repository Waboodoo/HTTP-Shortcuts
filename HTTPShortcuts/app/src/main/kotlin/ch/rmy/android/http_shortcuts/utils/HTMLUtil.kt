package ch.rmy.android.http_shortcuts.utils

import android.content.Context
import android.os.Build
import android.text.Html
import android.text.Spanned
import androidx.annotation.StringRes

object HTMLUtil {

    @Suppress("DEPRECATION")
    fun getHTML(context: Context, @StringRes stringRes: Int): Spanned {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(context.getString(stringRes), 0)
        }
        return Html.fromHtml(context.getString(stringRes))
    }

}

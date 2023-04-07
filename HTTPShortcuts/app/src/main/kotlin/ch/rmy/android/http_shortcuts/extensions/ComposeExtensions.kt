package ch.rmy.android.http_shortcuts.extensions

import android.content.Context
import android.os.Bundle
import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import ch.rmy.android.framework.utils.localization.Localizable
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

val shortTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)

@Stable
@Composable
fun Localizable.localize(): String =
    localize(LocalContext.current).toString()

@Stable
fun LocalDateTime.formatShortTime(): String =
    shortTimeFormatter.format(this)

@Composable
fun <T : WebView> rememberWebView(key: String, init: (Context, isRestore: Boolean) -> T): T {
    val context = LocalContext.current
    val webView = rememberSaveable(
        saver = object : Saver<T, Bundle> {
            override fun restore(value: Bundle): T =
                init(context, true)
                    .apply {
                        restoreState(value)
                    }

            override fun SaverScope.save(value: T): Bundle =
                Bundle()
                    .apply {
                        value.saveState(this)
                    }
        },
        key = key,
    ) {
        init(context, false)
    }
    return webView
}

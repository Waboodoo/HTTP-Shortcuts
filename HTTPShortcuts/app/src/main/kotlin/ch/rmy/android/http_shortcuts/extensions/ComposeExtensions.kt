package ch.rmy.android.http_shortcuts.extensions

import android.content.Context
import android.os.Bundle
import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import ch.rmy.android.framework.extensions.logInfo
import ch.rmy.android.framework.utils.WebViewChecker
import ch.rmy.android.framework.utils.localization.Localizable
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

val shortTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
val mediumTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM)
val mediumDateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)

@Stable
@Composable
fun Localizable.localize(): String =
    localize(LocalContext.current).toString()

@Stable
fun LocalDateTime.formatShortTime(): String =
    shortTimeFormatter.format(this)

@Stable
fun LocalDateTime.formatMediumTime(): String =
    mediumTimeFormatter.format(this)

@Stable
fun LocalDateTime.formatMediumDateTime(): String =
    mediumDateTimeFormatter.format(this)

@Stable
fun LocalDate.formatDate(): String =
    dateFormatter.format(this)

@Composable
fun <T : WebView> rememberWebView(init: (Context, isRestore: Boolean) -> T): T? {
    val isWebViewAvailable = remember { WebViewChecker.isWebViewAvailable() }
    if (!isWebViewAvailable) {
        return null
    }
    val context = LocalContext.current
    val webView = rememberSaveable(
        saver = object : Saver<T, Bundle> {
            override fun restore(value: Bundle): T =
                init(context, true)
                    .apply {
                        logInfo("Restoring web view state")
                        restoreState(value)
                    }

            override fun SaverScope.save(value: T): Bundle {
                logInfo("Saving web view state")
                val bundle = Bundle()
                value.saveState(bundle)

                // If the bundle is too big, it's safer to discard it to avoid TransactionTooLargeException
                val bytes = bundle.getByteArray("WEBVIEW_CHROMIUM_STATE")
                if (bytes != null && bytes.size > 200_000) {
                    logInfo("Ignoring web view state of ${bytes.size} bytes")
                    return Bundle()
                }
                logInfo("Saved web view state of ${bytes?.size} bytes")
                return bundle
            }
        },
    ) {
        init(context, false)
    }
    return webView
}

fun TextFieldValue.insertAtCursor(before: String = "", after: String = ""): TextFieldValue {
    val position = selection.min
    return copy(
        text = text.take(position) + before + after + text.drop(position),
        selection = TextRange(position + before.length),
        composition = null,
    )
}

inline fun Modifier.runIf(predicate: Boolean, block: Modifier.() -> Modifier): Modifier =
    if (predicate) block(this) else this

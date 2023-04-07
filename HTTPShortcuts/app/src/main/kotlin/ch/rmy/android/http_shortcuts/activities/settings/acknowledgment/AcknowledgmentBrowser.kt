package ch.rmy.android.http_shortcuts.activities.settings.acknowledgment

import android.annotation.SuppressLint
import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.viewinterop.NoOpUpdate
import ch.rmy.android.http_shortcuts.extensions.rememberWebView

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AcknowledgmentBrowser(
    onLoaded: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val webView = rememberWebView(key = "acknowledgment") { context, isRestore ->
        AcknowledgmentWebView(context, isRestore)
    }

    AndroidView(
        modifier = modifier,
        factory = {
            webView
        },
        update = {
            it.onLoaded = onLoaded
        },
        onReset = NoOpUpdate,
        onRelease = WebView::destroy,
    )
}

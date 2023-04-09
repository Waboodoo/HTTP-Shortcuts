package ch.rmy.android.http_shortcuts.activities.documentation

import android.annotation.SuppressLint
import android.net.Uri
import android.webkit.WebView
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.viewinterop.NoOpUpdate
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.rmy.android.http_shortcuts.extensions.rememberWebView

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DocumentationBrowser(
    url: Uri,
    onPageChanged: (Uri) -> Unit,
    onPageTitle: (String?) -> Unit,
    onLoadingStateChanged: (loading: Boolean) -> Unit,
    onExternalUrl: (Uri) -> Unit,
    modifier: Modifier = Modifier,
) {
    val webView = rememberWebView(key = "documentation") { context, _ ->
        DocumentationWebView(context)
    }

    val canGoBack by webView.canGoBack.collectAsStateWithLifecycle()

    BackHandler(enabled = canGoBack) {
        webView.goBack()
    }

    LaunchedEffect(url) {
        val internalUrl = DocumentationUrlManager.toInternalUrl(url)?.toString() ?: return@LaunchedEffect
        if (internalUrl != webView.url) {
            webView.loadUrl(internalUrl)
        }
    }

    AndroidView(
        modifier = modifier,
        factory = {
            webView
        },
        update = {
            it.showLoading = {
                onLoadingStateChanged(true)
            }
            it.hideLoading = {
                onLoadingStateChanged(false)
            }
            it.onPageChanged = onPageChanged
            it.onPageTitle = onPageTitle
            it.onExternalUrl = onExternalUrl
        },
        onReset = NoOpUpdate,
        onRelease = WebView::destroy,
    )
}

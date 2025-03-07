package ch.rmy.android.http_shortcuts.activities.documentation

import android.annotation.SuppressLint
import android.net.Uri
import android.webkit.WebView
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.viewinterop.NoOpUpdate
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.rmy.android.http_shortcuts.activities.documentation.models.SearchDirection
import ch.rmy.android.http_shortcuts.extensions.rememberWebView
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun DocumentationBrowser(
    url: Uri,
    searchQuery: String?,
    searchDirectionRequests: Flow<SearchDirection>,
    onPageChanged: (Uri) -> Unit,
    onPageTitle: (String?) -> Unit,
    onLoadingStateChanged: (loading: Boolean) -> Unit,
    onExternalUrl: (Uri) -> Unit,
    onSearchResults: (Int, Int) -> Unit,
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
    LaunchedEffect(webView) {
        webView.setFindListener { activeMatchOrdinal, numberOfMatches, _ ->
            onSearchResults(activeMatchOrdinal + if (numberOfMatches > 0) 1 else 0, numberOfMatches)
        }
    }
    LaunchedEffect(searchQuery) {
        if (searchQuery != null) {
            delay(200.milliseconds)
            webView.findAllAsync(searchQuery)
        } else {
            webView.findAllAsync("")
        }
    }
    LaunchedEffect(searchDirectionRequests) {
        searchDirectionRequests.collect {
            when (it) {
                SearchDirection.PREVIOUS -> webView.findNext(false)
                SearchDirection.NEXT -> webView.findNext(true)
            }
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

package ch.rmy.android.http_shortcuts.components

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.viewinterop.NoOpUpdate
import ch.rmy.android.framework.extensions.consume
import ch.rmy.android.framework.extensions.isDarkThemeEnabled
import ch.rmy.android.framework.extensions.openURL
import ch.rmy.android.http_shortcuts.extensions.rememberWebView
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun SinglePageBrowser(
    url: String,
    modifier: Modifier = Modifier,
) {
    val webView = rememberWebView(key = url) { context, isRestore ->
        SinglePageWebView(context, url, isRestore)
    }

    var loadingSpinnerVisible by remember {
        mutableStateOf(true)
    }
    var isLoading by remember {
        mutableStateOf(true)
    }
    LaunchedEffect(isLoading) {
        if (!isLoading) {
            delay(50.milliseconds)
            loadingSpinnerVisible = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(modifier),
        contentAlignment = Alignment.Center,
    ) {
        if (loadingSpinnerVisible) {
            LoadingIndicator()
        }

        AndroidView(
            modifier = Modifier
                .alpha(if (loadingSpinnerVisible) 0f else 1f),
            factory = {
                webView
            },
            update = {
                it.onLoaded = {
                    isLoading = false
                }
            },
            onReset = NoOpUpdate,
            onRelease = WebView::destroy,
        )
    }
}

@SuppressLint("SetJavaScriptEnabled", "ViewConstructor")
private class SinglePageWebView(context: Context, url: String, isRestore: Boolean) : WebView(context) {
    var onLoaded: () -> Unit = {}

    init {
        layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
        webViewClient = object : WebViewClient() {
            override fun onPageCommitVisible(view: WebView?, url: String?) {
                setBackgroundColor(Color.TRANSPARENT)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                setBackgroundColor(Color.TRANSPARENT)
                if (context.isDarkThemeEnabled()) {
                    evaluateJavascript("document.getElementById('root').className = 'dark';") {
                        onLoaded()
                    }
                } else {
                    onLoaded()
                }
            }

            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest) = consume {
                context.openURL(request.url)
            }
        }

        settings.javaScriptEnabled = true

        if (!isRestore) {
            loadUrl(url)
        }
    }
}

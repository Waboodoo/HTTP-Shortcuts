package ch.rmy.android.http_shortcuts.activities.response

import android.content.Context
import android.util.AttributeSet
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.viewinterop.NoOpUpdate
import ch.rmy.android.framework.extensions.consume
import ch.rmy.android.framework.extensions.openURL
import ch.rmy.android.framework.extensions.runIf
import ch.rmy.android.framework.extensions.tryOrIgnore
import ch.rmy.android.http_shortcuts.components.LoadingIndicator
import ch.rmy.android.http_shortcuts.extensions.rememberWebView
import ch.rmy.android.http_shortcuts.utils.UserAgentUtil
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ResponseBrowser(
    text: String,
    baseUrl: String?,
    modifier: Modifier = Modifier,
) {
    val webView = rememberWebView(key = "response") { context, _ ->
        ResponseWebView(context)
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
                webView.onLoaded = {
                    isLoading = false
                }
                webView.loadFromString(text, baseUrl)
                webView
            },
            update = NoOpUpdate,
            onReset = NoOpUpdate,
            onRelease = WebView::destroy,
        )
    }
}

class ResponseWebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : WebView(context, attrs) {

    var onLoaded: () -> Unit = {}

    init {
        webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String) = consume {
                context.openURL(url)
            }

            override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
                if (!request.isForMainFrame && request.url.path.equals("/favicon.ico")) {
                    return tryOrIgnore {
                        WebResourceResponse("image/png", null, null)
                    }
                }
                return null
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                onLoaded()
            }
        }

        with(settings) {
            cacheMode = WebSettings.LOAD_NO_CACHE
            javaScriptEnabled = false
            saveFormData = false
            allowContentAccess = false
            allowFileAccess = false
            allowFileAccessFromFileURLs = false
            allowUniversalAccessFromFileURLs = false
            userAgentString = UserAgentUtil.userAgent
        }
    }

    fun loadFromString(data: String, baseUrl: String?) {
        val url = baseUrl
            ?.runIf(!baseUrl.endsWith("/")) {
                plus("/")
            }
        loadDataWithBaseURL(url, data, "text/html", "UTF-8", null)
    }
}

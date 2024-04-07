package ch.rmy.android.http_shortcuts.activities.documentation

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.net.toUri
import ch.rmy.android.framework.extensions.isDarkThemeEnabled
import ch.rmy.android.framework.extensions.tryOrIgnore
import ch.rmy.android.http_shortcuts.utils.UserAgentProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@SuppressLint("SetJavaScriptEnabled")
class DocumentationWebView(context: Context) : WebView(context) {

    var onExternalUrl: (Uri) -> Unit = {}
    var showLoading: () -> Unit = {}
    var hideLoading: () -> Unit = {}
    var onPageChanged: (Uri) -> Unit = {}
    var onPageTitle: (String?) -> Unit = {}

    private val _canGoBack = MutableStateFlow(false)
    val canGoBack: StateFlow<Boolean> = _canGoBack.asStateFlow()

    init {
        layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
        webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                if (request.isForMainFrame) {
                    val externalUrl = DocumentationUrlManager.toExternal(request.url)
                    val internalUrl = DocumentationUrlManager.toInternalUrl(externalUrl)
                    if (internalUrl != null) {
                        loadUrl(internalUrl.toString())
                    } else {
                        onExternalUrl(externalUrl)
                    }
                    return true
                }
                return super.shouldOverrideUrlLoading(view, request)
            }

            override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
                if (!request.isForMainFrame && request.url.path.equals("/favicon.ico")) {
                    return tryOrIgnore {
                        WebResourceResponse("image/png", null, null)
                    }
                }
                return null
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                showLoading()
            }

            override fun onPageCommitVisible(view: WebView?, url: String?) {
                setBackgroundColor(Color.TRANSPARENT)
            }

            override fun onPageFinished(view: WebView, url: String) {
                setBackgroundColor(Color.TRANSPARENT)
                _canGoBack.value = canGoBack()
                onPageChanged(DocumentationUrlManager.toExternal(url.toUri()))
                if (context.isDarkThemeEnabled()) {
                    evaluateJavascript("document.getElementById('root').className = 'dark';") {
                        hideLoading()
                    }
                } else {
                    hideLoading()
                }
                evaluateJavascript("""document.getElementsByTagName("h1")[0].innerText""") { pageTitle ->
                    onPageTitle(pageTitle.trim('"').takeUnless { it.isEmpty() || it == "null" || it == "Documentation" })
                }
            }
        }

        with(settings) {
            cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
            javaScriptEnabled = true
            allowContentAccess = false
            allowFileAccess = false
            userAgentString = UserAgentProvider.getUserAgent(context)
            blockNetworkLoads = true
            blockNetworkImage = false
            setSupportZoom(true)
            displayZoomControls = false
        }
    }
}

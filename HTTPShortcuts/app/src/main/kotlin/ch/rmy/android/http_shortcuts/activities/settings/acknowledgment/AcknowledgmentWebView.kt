package ch.rmy.android.http_shortcuts.activities.settings.acknowledgment

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import ch.rmy.android.framework.extensions.consume
import ch.rmy.android.framework.extensions.isDarkThemeEnabled
import ch.rmy.android.framework.extensions.openURL

@SuppressLint("SetJavaScriptEnabled")
class AcknowledgmentWebView(context: Context, isRestore: Boolean) : WebView(context) {
    var onLoaded: () -> Unit = {}

    init {
        layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
        webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
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
            loadUrl(ACKNOWLEDGMENTS_ASSET_URL)
        }
    }

    companion object {

        private const val ACKNOWLEDGMENTS_ASSET_URL = "file:///android_asset/acknowledgments.html"
    }
}

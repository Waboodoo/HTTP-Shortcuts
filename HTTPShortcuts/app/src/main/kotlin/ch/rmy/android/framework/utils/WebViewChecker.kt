package ch.rmy.android.framework.utils

import android.webkit.WebView

object WebViewChecker {
    fun isWebViewAvailable(): Boolean =
        try {
            // Try to access a harmless method on the WebView. This will fail if no WebView is installed.
            WebView.setWebContentsDebuggingEnabled(false)
            true
        } catch (e: Exception) {
            false
        }
}

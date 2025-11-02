package ch.rmy.android.framework.utils

import android.webkit.CookieManager

object WebViewChecker {
    fun isWebViewAvailable(): Boolean =
        try {
            // Try to access a harmless method on the WebView. This will fail if no WebView is installed.
            CookieManager.getInstance()
            true
        } catch (_: Exception) {
            false
        }
}

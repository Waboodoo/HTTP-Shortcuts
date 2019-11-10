package ch.rmy.android.http_shortcuts.views

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.webkit.WebView
import androidx.core.text.TextUtilsCompat.htmlEncode
import ch.rmy.android.http_shortcuts.extensions.isDarkThemeEnabled

class SyntaxHighlightView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : WebView(context, attrs, defStyleAttr) {

    init {
        setBackgroundColor(Color.TRANSPARENT)
    }

    fun setCode(content: String, language: String) {
        settings.javaScriptEnabled = true
        val html = """
            <html>
                <head>
                    <link rel="stylesheet" href="file:///android_asset/highlight/styles/${getStyle()}.css">
                    <link rel="stylesheet" href="file:///android_asset/highlight/style-override.css">
                    <script src="file:///android_asset/highlight/highlight.pack.js"></script>
                    <script>hljs.initHighlightingOnLoad();</script>
                </head>
                <body>
                    <pre>
                        <code class="$language">${htmlEncode(content)}</code>
                    </pre>
                </body>
            </html>
            """.trimIndent()
        loadDataWithBaseURL("file:///android_asset/", html, "text/html", "UTF-8", "")
    }

    fun getStyle() = if (context.isDarkThemeEnabled()) {
        "darcula"
    } else {
        "default"
    }

}

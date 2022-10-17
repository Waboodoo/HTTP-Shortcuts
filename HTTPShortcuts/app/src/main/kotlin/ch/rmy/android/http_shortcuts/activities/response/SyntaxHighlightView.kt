package ch.rmy.android.http_shortcuts.activities.response

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.webkit.WebView
import androidx.core.text.TextUtilsCompat.htmlEncode
import ch.rmy.android.framework.extensions.isDarkThemeEnabled
import ch.rmy.android.framework.extensions.tryOrLog
import ch.rmy.android.http_shortcuts.utils.GsonUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@SuppressLint("SetJavaScriptEnabled")
class SyntaxHighlightView
@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : WebView(context, attrs, defStyleAttr) {

    init {
        setBackgroundColor(Color.TRANSPARENT)
        settings.javaScriptEnabled = true
    }

    suspend fun setCode(content: String, language: Language) {
        if (language == Language.JSON) {
            tryOrLog {
                val prettyJson = withContext(Dispatchers.Default) {
                    GsonUtil.prettyPrint(content)
                }
                withContext(Dispatchers.Main) {
                    apply(prettyJson, language.language)
                }
            }
        } else {
            apply(content, language.language)
        }
    }

    private fun apply(content: String, language: String) {
        val html = """
            <html>
                <head>
                    <link rel="stylesheet" href="file:///android_asset/highlight/styles/${getStyle()}.css">
                    <link rel="stylesheet" href="file:///android_asset/highlight/style-override.css">
                    <script src="file:///android_asset/highlight/highlight.pack.js"></script>
                    <script>
                        let wrapLines = false;
                        document.addEventListener("click", function() {
                            wrapLines = !wrapLines;
                            const body = document.getElementById("body");
                            if (wrapLines) {
                                body.classList.add('wrap-lines');
                            } else {
                                body.classList.remove('wrap-lines');                            
                            }
                        });                    
                        hljs.initHighlightingOnLoad();
                    </script>
                </head>
                <body id="body">
                    <pre>
                        <code class="$language">${htmlEncode(content)}</code>
                    </pre>
                </body>
            </html>
        """.trimIndent()
        loadDataWithBaseURL("file:///android_asset/", html, "text/html", "UTF-8", "")
    }

    private fun getStyle() = if (context.isDarkThemeEnabled()) {
        "darcula"
    } else {
        "default"
    }

    enum class Language(val language: String) {
        XML("xml"),
        JSON("json"),
        YAML("yaml")
    }
}

package ch.rmy.android.http_shortcuts.activities.response

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.webkit.WebView
import androidx.core.text.TextUtilsCompat.htmlEncode
import ch.rmy.android.http_shortcuts.extensions.isDarkThemeEnabled
import ch.rmy.android.http_shortcuts.extensions.logException
import ch.rmy.android.http_shortcuts.utils.GsonUtil
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class SyntaxHighlightView
@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : WebView(context, attrs, defStyleAttr) {

    private var disposable: Disposable? = null

    init {
        setBackgroundColor(Color.TRANSPARENT)
        settings.javaScriptEnabled = true
    }

    fun setCode(content: String, language: Language) {
        disposable?.dispose()
        if (language == Language.JSON) {
            disposable = Single.fromCallable {
                GsonUtil.prettyPrint(content)
            }
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ prettyJson ->
                    apply(prettyJson, language.language)
                }, ::logException)
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

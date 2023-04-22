package ch.rmy.android.http_shortcuts.utils

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import com.wakaztahir.codeeditor.highlight.prettify.PrettifyParser
import com.wakaztahir.codeeditor.highlight.theme.CodeTheme

class SyntaxHighlighter(val language: String, val theme: CodeTheme) {

    private val parser = PrettifyParser()

    fun format(text: String): AnnotatedString =
        buildAnnotatedString {
            append(text)
            parser.parse("js", text).forEach {
                addStyle(theme.toSpanStyle(it), it.offset, it.offset + it.length)
            }
        }
}

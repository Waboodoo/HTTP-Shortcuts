package ch.rmy.android.http_shortcuts.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import com.wakaztahir.codeeditor.highlight.prettify.PrettifyParser
import com.wakaztahir.codeeditor.highlight.theme.CodeTheme
import com.wakaztahir.codeeditor.highlight.theme.DefaultTheme
import com.wakaztahir.codeeditor.highlight.theme.SyntaxColors

class SyntaxHighlighter(
    private val language: String,
    useDarkTheme: Boolean,
) {

    private val theme = if (useDarkTheme) {
        DarkTheme
    } else {
        DefaultTheme()
    }

    private val parser = PrettifyParser()

    fun format(text: String): AnnotatedString =
        buildAnnotatedString {
            append(text)
            applyFormatting(this, text)
        }

    fun applyFormatting(builder: AnnotatedString.Builder, text: String) {
        with(builder) {
            parser.parse(language, text).forEach {
                addStyle(theme.toSpanStyle(it), it.offset, it.offset + it.length)
            }
        }
    }

    private object DarkTheme : CodeTheme() {
        override val colors = SyntaxColors(
            type = Color(0xFF1cc4ae),
            keyword = Color(0xFF1cc4ae),
            literal = Color(0xFFbfb9b0),
            comment = Color(0xFF877d6e),
            string = Color(0xFFc27905),
            punctuation = Color(0xFF70685c),
            plain = Color(0xFFfff5e6),
            tag = Color(0xFF7989f1),
            declaration = Color(0xFFbdb3b6),
            source = Color(0xFFc27905),
            attrName = Color(0xFF929ff7),
            attrValue = Color(0xFFc27905),
            nocode = Color(0xFFfff5e6),
        )
    }

    object Languages {
        const val JS = "js"
    }
}

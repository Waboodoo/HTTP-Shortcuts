package ch.rmy.android.http_shortcuts.utils

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.wakaztahir.codeeditor.highlight.prettify.PrettifyParser
import com.wakaztahir.codeeditor.highlight.theme.CodeTheme
import com.wakaztahir.codeeditor.highlight.theme.DefaultTheme
import com.wakaztahir.codeeditor.highlight.theme.SyntaxColors
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class SyntaxHighlighter(
    private val language: String,
    useDarkTheme: Boolean,
) {

    private val theme = if (useDarkTheme) {
        DarkTheme
    } else {
        DefaultTheme()
    }

    fun applyFormatting(builder: AnnotatedString.Builder, text: String) {
        with(builder) {
            parser.parse(language, text).forEach {
                addStyle(theme.toSpanStyle(it), it.offset, it.offset + it.length)
            }
        }
    }

    fun getFormattingTransformation(text: String): (builder: AnnotatedString.Builder) -> Unit {
        val parseResult = parser.parse(language, text)
        return { builder ->
            with(builder) {
                parseResult.forEach {
                    addStyle(theme.toSpanStyle(it), it.offset, it.offset + it.length)
                }
            }
        }
    }

    private object DarkTheme : CodeTheme() {
        override val colors = SyntaxColors(
            type = Color(0xFF1FEED3),
            keyword = Color(0xFF1BEBCF),
            literal = Color(0xFFbfb9b0),
            comment = Color(0xFFFFC263),
            string = Color(0xFFc27905),
            punctuation = Color(0xFFB4A794),
            plain = Color(0xFFfff5e6),
            tag = Color(0xFF6479FF),
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

    companion object {
        private val parserDeferred: Deferred<PrettifyParser> =
            CoroutineScope(Dispatchers.Default).async {
                PrettifyParser()
            }

        internal val parser
            get() = runBlocking { parserDeferred.await() }
    }
}

@Composable
fun rememberSyntaxHighlighter(language: String): SyntaxHighlighter {
    val useDarkTheme = isSystemInDarkTheme()
    return remember(language, useDarkTheme) {
        SyntaxHighlighter(language, useDarkTheme)
    }
}

private val FORMATTING_DELAY = 300.milliseconds
private const val FORMATTING_DELAY_THRESHOLD = 2000

@Composable
fun syntaxHighlightingVisualTransformation(syntaxHighlighter: SyntaxHighlighter, value: String): VisualTransformation {
    var transformation: (AnnotatedString.Builder) -> Unit by remember {
        mutableStateOf(syntaxHighlighter.getFormattingTransformation(value))
    }
    val coroutineScope = rememberCoroutineScope()
    DisposableEffect(syntaxHighlighter, value) {
        val job = coroutineScope.launch(Dispatchers.Default) {
            if (value.length > FORMATTING_DELAY_THRESHOLD) {
                delay(FORMATTING_DELAY)
            }
            transformation = syntaxHighlighter.getFormattingTransformation(value)
        }
        onDispose {
            job.cancel()
        }
    }

    return transformation.let { transform ->
        VisualTransformation { text ->
            val formatted = buildAnnotatedString {
                append(text)
                transform(this)
            }
            TransformedText(formatted, OffsetMapping.Identity)
        }
    }
}

package ch.rmy.android.http_shortcuts.components

import android.content.Context
import android.graphics.Typeface
import android.text.style.ForegroundColorSpan
import android.text.style.ImageSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.SubscriptSpan
import android.text.style.SuperscriptSpan
import android.text.style.URLSpan
import android.text.style.UnderlineSpan
import android.util.Base64
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import ch.rmy.android.framework.extensions.openURL
import ch.rmy.android.http_shortcuts.http.HttpHeaders
import ch.rmy.android.http_shortcuts.utils.HTMLUtil
import ch.rmy.android.http_shortcuts.utils.UserAgentProvider
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun HtmlRichText(
    text: String,
    monospace: Boolean = false,
) {
    val context = LocalContext.current
    val linkColor = MaterialTheme.colorScheme.primary
    var annotatedString by remember {
        mutableStateOf(AnnotatedString(""))
    }
    var imageMap by remember {
        mutableStateOf<Map<String, Image>>(emptyMap())
    }
    var isLoading by remember {
        mutableStateOf(true)
    }
    var maxWidth by remember {
        mutableFloatStateOf(0f)
    }

    BoxWithConstraints(
        Modifier.fillMaxWidth(),
    ) {
        maxWidth = with(LocalDensity.current) {
            this@BoxWithConstraints.maxWidth.toPx()
        }
    }

    LaunchedEffect(text, linkColor, maxWidth) {
        if (maxWidth == 0f) {
            return@LaunchedEffect
        }
        val (annotatedText, images) = withContext(Dispatchers.Default) {
            toAnnotatedString(context, text, linkColor, maxWidth)
        }
        annotatedString = annotatedText
        imageMap = images
        isLoading = false
    }

    val inlineContentMap = remember(imageMap) {
        imageMap.mapValues { (_, image) ->
            val width = pxToSp(context, image.width)
            val height = pxToSp(context, image.height)

            InlineTextContent(
                placeholder = Placeholder(width, height, PlaceholderVerticalAlign.Top),
                children = {
                    AsyncImage(
                        model = image.data,
                        modifier = Modifier.fillMaxSize(),
                        contentDescription = null,
                    )
                },
            )
        }
    }

    val onClick = { offset: Int ->
        annotatedString.getStringAnnotations(tag = URL_TAG, start = offset, end = offset).firstOrNull()?.let {
            context.openURL(it.item)
        }
    }
    val layoutResult = remember { mutableStateOf<TextLayoutResult?>(null) }
    val modifier = Modifier.pointerInput(onClick) {
        detectTapGestures { position ->
            layoutResult.value?.let { layoutResult ->
                onClick(layoutResult.getOffsetForPosition(position))
            }
        }
    }

    if (isLoading) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CircularProgressIndicator()
        }
    } else {
        Text(
            text = annotatedString,
            inlineContent = inlineContentMap,
            modifier = modifier,
            fontFamily = if (monospace) FontFamily.Monospace else null,
            onTextLayout = {
                layoutResult.value = it
            },
        )
    }
}

private fun pxToSp(context: Context, px: Float): TextUnit =
    (px / context.resources.displayMetrics.scaledDensity).sp

private suspend fun toAnnotatedString(
    context: Context,
    string: String,
    linkColor: Color,
    maxWidth: Float,
): Pair<AnnotatedString, Map<String, Image>> {
    val spanned = HTMLUtil.toSpanned(string)

    val images = mutableMapOf<String, Image>()
    var idCounter = 0

    val annotatedString = buildAnnotatedString {
        fun appendTextWithAndCopyStyling(start: Int, end: Int, offset: Int) {
            append(spanned, start, end)
            spanned.getSpans(start, end, Any::class.java)
                .forEach { span ->
                    val spanStart = spanned.getSpanStart(span) - offset
                    val spanEnd = spanned.getSpanEnd(span) - offset
                    val style = when (span) {
                        is ForegroundColorSpan -> SpanStyle(color = Color(span.foregroundColor))
                        is StyleSpan -> when (span.style) {
                            Typeface.ITALIC -> SpanStyle(fontStyle = FontStyle.Italic)
                            Typeface.BOLD -> SpanStyle(fontWeight = FontWeight.Bold)
                            Typeface.BOLD_ITALIC -> SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic)
                            else -> null
                        }
                        is SuperscriptSpan -> SpanStyle(baselineShift = BaselineShift.Superscript)
                        is SubscriptSpan -> SpanStyle(baselineShift = BaselineShift.Subscript)
                        is UnderlineSpan -> SpanStyle(textDecoration = TextDecoration.Underline)
                        is StrikethroughSpan -> SpanStyle(textDecoration = TextDecoration.LineThrough)
                        is URLSpan -> SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline)
                        else -> null
                    }
                    style?.let { addStyle(it, spanStart, spanEnd) }
                    when (span) {
                        is URLSpan -> addStringAnnotation(tag = URL_TAG, span.url, spanStart, spanEnd)
                    }
                }
        }

        var offset = 0
        var previousEnd = 0
        spanned.getSpans(0, spanned.length, ImageSpan::class.java)
            .sortedBy { span ->
                spanned.getSpanStart(span)
            }
            .forEach { imageSpan ->
                val spanStart = spanned.getSpanStart(imageSpan)
                val spanEnd = spanned.getSpanEnd(imageSpan)

                appendTextWithAndCopyStyling(previousEnd, spanStart, offset)

                imageSpan.source
                    ?.let { source ->
                        loadImage(context, source)
                    }
                    ?.let { image ->
                        val id = "image${idCounter++}"

                        val originalWidth = image.width
                        val originalHeight = image.height
                        val aspectRatio = originalWidth / originalHeight
                        val finalWidth = originalWidth.coerceAtMost(maxWidth)
                        val finalHeight = finalWidth / aspectRatio

                        images[id] = image.copy(
                            width = finalWidth,
                            height = finalHeight,
                        )
                        appendInlineContent(id, image.source)
                        addStyle(
                            ParagraphStyle(
                                lineHeight = pxToSp(context, finalHeight),
                            ),
                            length,
                            length,
                        )
                        offset -= image.source.length
                    }

                offset += spanEnd - spanStart
                previousEnd = spanEnd
            }

        appendTextWithAndCopyStyling(previousEnd, spanned.length, offset)
    }

    return Pair(annotatedString, images)
}

private suspend fun loadImage(context: Context, source: String): Image? {
    val transformedSource = if (source.isBase64EncodedImage()) {
        source.getBase64ImageData()
    } else {
        source
    }
        ?: return null

    val imageResult = ImageLoader(context)
        .execute(
            ImageRequest.Builder(context)
                .addHeader(HttpHeaders.USER_AGENT, UserAgentProvider.getUserAgent(context))
                .data(transformedSource)
                .build(),
        )
    return Image(
        source,
        transformedSource,
        width = imageResult.drawable?.intrinsicWidth?.toFloat() ?: return null,
        height = imageResult.drawable?.intrinsicHeight?.toFloat() ?: return null,
    )
}

private fun String.isBase64EncodedImage(): Boolean =
    matches("^data:image/[^;]+;base64,.+".toRegex(RegexOption.IGNORE_CASE))

private fun String.getBase64ImageData(): ByteArray? =
    try {
        dropWhile { it != ',' }
            .drop(1)
            .let {
                Base64.decode(it, Base64.DEFAULT)
            }
    } catch (e: IllegalArgumentException) {
        null
    }

private data class Image(val source: String, val data: Any, val width: Float, val height: Float)

private const val URL_TAG = "url"

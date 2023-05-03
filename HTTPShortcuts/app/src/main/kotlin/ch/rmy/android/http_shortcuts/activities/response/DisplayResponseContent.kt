package ch.rmy.android.http_shortcuts.activities.response

import android.net.Uri
import android.text.format.Formatter
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.response.models.DetailInfo
import ch.rmy.android.http_shortcuts.components.FontSize
import ch.rmy.android.http_shortcuts.components.Image
import ch.rmy.android.http_shortcuts.components.Spacing
import ch.rmy.android.http_shortcuts.utils.FileTypeUtil
import ch.rmy.android.http_shortcuts.utils.SyntaxHighlighter

@Composable
fun DisplayResponseContent(
    detailInfo: DetailInfo?,
    text: String,
    mimeType: String?,
    fileUri: Uri?,
    url: Uri?,
    limitExceeded: Long?,
) {
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(Spacing.SMALL),
    ) {
        if (detailInfo != null) {
            DetailInfoCards(detailInfo)
        }

        ResponseDisplay(
            text = text,
            mimeType = mimeType,
            fileUri = fileUri,
            url = url,
            limitExceeded = limitExceeded,
        )
    }
}

@Composable
private fun DetailInfoCards(detailInfo: DetailInfo) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = Spacing.MEDIUM,
                start = Spacing.MEDIUM,
                end = Spacing.MEDIUM,
            ),
        verticalArrangement = Arrangement.spacedBy(Spacing.SMALL),
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(Spacing.MEDIUM),
            ) {
                Text(
                    stringResource(R.string.label_general_response_info),
                    fontSize = FontSize.BIG,
                )
                detailInfo.status?.let {
                    KeyValueText(stringResource(R.string.label_status_code), it)
                }
                detailInfo.url?.let {
                    KeyValueText(stringResource(R.string.label_response_url), it)
                }
                detailInfo.timing?.let {
                    val milliseconds = it.inWholeMilliseconds.toInt()
                    KeyValueText(
                        stringResource(R.string.label_response_timing),
                        pluralStringResource(
                            R.plurals.milliseconds,
                            milliseconds,
                            milliseconds,
                        )
                    )
                }
            }
        }
        if (detailInfo.headers.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(Spacing.MEDIUM),
                ) {
                    Text(
                        stringResource(R.string.label_response_headers),
                        fontSize = FontSize.BIG,
                    )
                    detailInfo.headers.forEach { (name, value) ->
                        KeyValueText(name, value)
                    }
                }
            }
        }
    }
}

@Composable
private fun KeyValueText(key: String, value: String) {
    Text(
        modifier = Modifier.fillMaxWidth(),
        text = buildAnnotatedString {
            append(key)
            append(": ")
            addStyle(SpanStyle(fontWeight = FontWeight.Bold), 0, key.length + 1)
            append(value)
        },
        fontSize = FontSize.SMALL,
    )
}

@Composable
private fun ResponseDisplay(
    text: String,
    mimeType: String?,
    fileUri: Uri?,
    url: Uri?,
    limitExceeded: Long?,
) {
    if (FileTypeUtil.isImage(mimeType)) {
        Image(
            modifier = Modifier.fillMaxWidth(),
            uri = fileUri!!,
            preventMemoryCache = true,
        )
        return
    }
    if (limitExceeded != null) {
        val context = LocalContext.current
        PlainText(stringResource(R.string.error_response_too_large, Formatter.formatShortFileSize(context, limitExceeded)), italic = true)
        return
    }
    if (text.isBlank()) {
        PlainText(stringResource(R.string.message_blank_response), italic = true)
        return
    }
    when (mimeType) {
        FileTypeUtil.TYPE_HTML -> {
            ResponseBrowser(text, url?.toString())
        }
        FileTypeUtil.TYPE_JSON -> {
            SyntaxHighlightedText(text, language = "json")
        }
        FileTypeUtil.TYPE_XML -> {
            SyntaxHighlightedText(text, language = "xml")
        }
        FileTypeUtil.TYPE_YAML, FileTypeUtil.TYPE_YAML_ALT -> {
            SyntaxHighlightedText(text, language = "yml")
        }
        else -> {
            PlainText(text)
        }
    }
}

@Composable
private fun PlainText(text: String, italic: Boolean = false) {
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.MEDIUM),
        text = text,
        fontStyle = if (italic) FontStyle.Italic else FontStyle.Normal,
    )
}

@Composable
private fun SyntaxHighlightedText(text: String, language: String) {
    val useDarkTheme = isSystemInDarkTheme()
    val syntaxHighlighter = remember(language, useDarkTheme) {
        SyntaxHighlighter(language, useDarkTheme)
    }
    // TODO: Handle overflow
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.MEDIUM),
        text = syntaxHighlighter.format(text),
        style = TextStyle(
            fontFamily = FontFamily.Monospace,
        ),
    )
}

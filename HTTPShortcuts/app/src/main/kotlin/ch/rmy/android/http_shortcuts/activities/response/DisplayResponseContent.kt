package ch.rmy.android.http_shortcuts.activities.response

import android.net.Uri
import android.text.format.Formatter
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import androidx.compose.ui.text.style.TextOverflow
import ch.rmy.android.framework.extensions.openURL
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.response.models.DetailInfo
import ch.rmy.android.http_shortcuts.components.FontSize
import ch.rmy.android.http_shortcuts.components.Spacing
import ch.rmy.android.http_shortcuts.extensions.rememberSyntaxHighlighter
import ch.rmy.android.http_shortcuts.extensions.runIf
import ch.rmy.android.http_shortcuts.http.HttpHeaders
import ch.rmy.android.http_shortcuts.utils.FileTypeUtil
import ch.rmy.android.http_shortcuts.utils.UserAgentProvider
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest

@Composable
fun DisplayResponseContent(
    detailInfo: DetailInfo?,
    text: String,
    mimeType: String?,
    fileUri: Uri?,
    url: Uri?,
    limitExceeded: Long?,
    monospace: Boolean,
    showExternalUrlWarning: Boolean,
    onExternalUrlWarningHidden: (Boolean) -> Unit,
) {
    Column {
        if (detailInfo != null) {
            DetailInfoCards(
                detailInfo,
                modifier = Modifier.weight(1f, fill = false)
            )
        }

        Box(
            modifier = Modifier.weight(3f, fill = false)
        ) {
            ResponseDisplay(
                text = text,
                mimeType = mimeType,
                fileUri = fileUri,
                url = url,
                limitExceeded = limitExceeded,
                monospace = monospace,
                showExternalUrlWarning = showExternalUrlWarning,
                onExternalUrlWarningHidden = onExternalUrlWarningHidden,
            )
        }
    }
}

@Composable
private fun DetailInfoCards(
    detailInfo: DetailInfo,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(
                horizontal = Spacing.MEDIUM,
                vertical = Spacing.SMALL,
            )
            .then(modifier),
        verticalArrangement = Arrangement.spacedBy(Spacing.SMALL),
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            SelectionContainer {
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
        }
        if (detailInfo.headers.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(),
            ) {
                SelectionContainer {
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
    monospace: Boolean,
    showExternalUrlWarning: Boolean,
    onExternalUrlWarningHidden: (Boolean) -> Unit,
) {
    val context = LocalContext.current
    if (FileTypeUtil.isImage(mimeType)) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .addHeader(HttpHeaders.USER_AGENT, UserAgentProvider.getUserAgent(context))
                .data(fileUri)
                .diskCachePolicy(CachePolicy.DISABLED)
                .memoryCachePolicy(CachePolicy.DISABLED)
                .build(),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth(),
        )
        return
    }
    if (limitExceeded != null) {
        PlainText(stringResource(R.string.error_response_too_large, Formatter.formatShortFileSize(context, limitExceeded)), italic = true)
        return
    }
    if (text.isBlank()) {
        PlainText(stringResource(R.string.message_blank_response), italic = true)
        return
    }
    when (mimeType) {
        FileTypeUtil.TYPE_HTML -> {
            var externalUrl by remember {
                mutableStateOf<Uri?>(null)
            }
            ResponseBrowser(
                text,
                url?.toString(),
                onExternalUrl = {
                    if (showExternalUrlWarning) {
                        externalUrl = it
                    } else {
                        context.openURL(it)
                    }
                }
            )
            externalUrl?.let {
                OpenExternalUrlDialog(
                    url = it,
                    onConfirm = {
                        externalUrl = null
                        context.openURL(it)
                    },
                    onDoNotShowAgain = onExternalUrlWarningHidden,
                    onDismissed = {
                        externalUrl = null
                    }
                )
            }
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
            PlainText(text, monospace)
        }
    }
}

@Composable
private fun PlainText(text: String, monospace: Boolean = false, italic: Boolean = false) {
    Box(modifier = Modifier.verticalScroll(rememberScrollState())) {
        SelectionContainer {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.MEDIUM),
                text = text,
                fontFamily = if (monospace) FontFamily.Monospace else null,
                fontStyle = if (italic) FontStyle.Italic else FontStyle.Normal,
            )
        }
    }
}

@Composable
private fun SyntaxHighlightedText(text: String, language: String) {
    var wrapLines by rememberSaveable(key = "wrap-lines") {
        mutableStateOf(true)
    }
    val scrollState = rememberScrollState()

    val syntaxHighlighter = rememberSyntaxHighlighter(language)

    Box(modifier = Modifier.verticalScroll(rememberScrollState())) {
        SelectionContainer {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .runIf(!wrapLines) {
                        horizontalScroll(scrollState)
                    }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            wrapLines = !wrapLines
                        },
                    )
                    .padding(Spacing.MEDIUM),
                text = syntaxHighlighter.format(text),
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                ),
            )
        }
    }
}

@Composable
private fun OpenExternalUrlDialog(
    url: Uri,
    onDoNotShowAgain: (Boolean) -> Unit,
    onConfirm: () -> Unit,
    onDismissed: () -> Unit,
) {
    var permanentlyHidden by remember {
        mutableStateOf(false)
    }
    AlertDialog(
        onDismissRequest = onDismissed,
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(Spacing.SMALL),
            ) {
                Text(stringResource(R.string.warning_page_wants_to_open_url, url))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            permanentlyHidden = !permanentlyHidden
                            onDoNotShowAgain(permanentlyHidden)
                        }
                        .padding(Spacing.TINY),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.SMALL, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = permanentlyHidden,
                        onCheckedChange = null,
                    )
                    Text(
                        stringResource(R.string.dialog_checkbox_do_not_show_again),
                        fontSize = FontSize.MEDIUM,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.dialog_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissed) {
                Text(stringResource(R.string.dialog_cancel))
            }
        }
    )
}

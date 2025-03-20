package ch.rmy.android.http_shortcuts.activities.response

import android.content.ActivityNotFoundException
import android.net.Uri
import android.text.format.Formatter
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isUnspecified
import androidx.compose.ui.unit.sp
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.extensions.logInfo
import ch.rmy.android.framework.extensions.openURL
import ch.rmy.android.framework.extensions.showToast
import ch.rmy.android.framework.extensions.truncate
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.response.models.DetailInfo
import ch.rmy.android.http_shortcuts.activities.response.models.TableData
import ch.rmy.android.http_shortcuts.components.FontSize
import ch.rmy.android.http_shortcuts.components.LoadingIndicator
import ch.rmy.android.http_shortcuts.components.Spacing
import ch.rmy.android.http_shortcuts.extensions.runIf
import ch.rmy.android.http_shortcuts.http.HttpHeaders
import ch.rmy.android.http_shortcuts.utils.FileTypeUtil
import ch.rmy.android.http_shortcuts.utils.UserAgentProvider
import ch.rmy.android.http_shortcuts.utils.rememberSyntaxHighlighter
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.sunnychung.lib.android.composabletable.ux.Table
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable

private const val TAG = "DisplayResponseContent"

@Composable
fun DisplayResponseContent(
    detailInfo: DetailInfo?,
    text: String,
    mimeType: String?,
    fileUri: Uri?,
    url: Uri?,
    limitExceeded: Long?,
    monospace: Boolean,
    fontSize: Int?,
    showExternalUrlWarning: Boolean,
    tableData: TableData?,
    javaScriptEnabled: Boolean,
    processing: Boolean,
    onExternalUrlWarningHidden: (Boolean) -> Unit,
) {
    var detailsExpanded by remember {
        mutableStateOf(false)
    }
    Column {
        if (detailInfo != null) {
            DetailsHeader(
                modifier = Modifier.fillMaxWidth(),
                onClicked = {
                    detailsExpanded = !detailsExpanded
                },
                expanded = detailsExpanded,
            )
        }

        Box(
            modifier = Modifier.weight(1f),
        ) {
            ResponseDisplay(
                text = text,
                mimeType = mimeType,
                fileUri = fileUri,
                url = url,
                limitExceeded = limitExceeded,
                monospace = monospace,
                fontSize = fontSize?.sp ?: TextUnit.Unspecified,
                showExternalUrlWarning = showExternalUrlWarning,
                tableData = tableData,
                processing = processing,
                javaScriptEnabled = javaScriptEnabled,
                onExternalUrlWarningHidden = onExternalUrlWarningHidden,
            )

            if (detailInfo != null) {
                androidx.compose.animation.AnimatedVisibility(
                    modifier = Modifier,
                    visible = detailsExpanded,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically(),
                ) {
                    DetailInfoCards(
                        detailInfo = detailInfo,
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailsHeader(
    modifier: Modifier,
    expanded: Boolean,
    onClicked: () -> Unit,
) {
    val rotationDegrees by animateFloatAsState(targetValue = if (expanded) 90f else 0f)
    Column(modifier) {
        HorizontalDivider()
        ListItem(
            modifier = Modifier.clickable(onClick = onClicked),
            headlineContent = {
                Text(stringResource(R.string.title_response_meta_information))
            },
            trailingContent = {
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier
                        .rotate(rotationDegrees),
                )
            },
        )
    }
}

@Composable
private fun DetailInfoCards(
    detailInfo: DetailInfo,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(
                horizontal = Spacing.MEDIUM,
                vertical = Spacing.SMALL,
            )
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
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
                            ),
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
    fontSize: TextUnit,
    showExternalUrlWarning: Boolean,
    processing: Boolean,
    javaScriptEnabled: Boolean,
    tableData: TableData?,
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
                .fillMaxWidth()
                .zoomable(rememberZoomState()),
        )
        return
    }
    if (limitExceeded != null) {
        PlainText(
            stringResource(R.string.error_response_too_large, Formatter.formatShortFileSize(context, limitExceeded)),
            italic = true,
            fontSize = fontSize,
        )
        return
    }
    if (text.isBlank()) {
        PlainText(
            stringResource(R.string.message_blank_response),
            italic = true,
            fontSize = fontSize,
        )
        return
    }
    when (mimeType) {
        FileTypeUtil.TYPE_HTML -> {
            var externalUrl by remember {
                mutableStateOf<Uri?>(null)
            }
            ResponseBrowser(
                text = text,
                baseUrl = url?.toString(),
                javaScriptEnabled = javaScriptEnabled,
                onExternalUrl = {
                    if (showExternalUrlWarning) {
                        externalUrl = it
                    } else {
                        try {
                            logInfo(TAG, "Opening URL: $it")
                            context.openURL(it)
                        } catch (e: ActivityNotFoundException) {
                            logException(TAG, e)
                            context.showToast("No app found to open URL")
                        } catch (e: SecurityException) {
                            logException(TAG, e)
                            context.showToast("Missing permission, can't open URL")
                        }
                    }
                },
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
                    },
                )
            }
        }
        FileTypeUtil.TYPE_JSON -> {
            if (tableData != null) {
                TableView(tableData, fontSize)
            } else if (!processing && text.isNotEmpty()) {
                SyntaxHighlightedText(text, language = "json", fontSize = fontSize)
            } else {
                LoadingIndicator()
            }
        }
        FileTypeUtil.TYPE_XML -> {
            SyntaxHighlightedText(text, language = "xml", fontSize = fontSize)
        }
        FileTypeUtil.TYPE_YAML, FileTypeUtil.TYPE_YAML_ALT -> {
            SyntaxHighlightedText(text, language = "yml", fontSize = fontSize)
        }
        else -> {
            PlainText(text, monospace, fontSize = fontSize)
        }
    }
}

@Composable
private fun PlainText(text: String, monospace: Boolean = false, italic: Boolean = false, fontSize: TextUnit) {
    val fontFamily = if (monospace) FontFamily.Monospace else null
    val fontStyle = if (italic) FontStyle.Italic else FontStyle.Normal
    Box(modifier = Modifier.verticalScroll(rememberScrollState())) {
        SelectionContainer {
            Column(
                modifier = Modifier.padding(Spacing.MEDIUM),
            ) {
                // Somehow, verticalScroll starts falling apart and crashing when the text
                // composable is too big, so we split it up into smaller chunks
                text
                    .truncate(120_000)
                    .split("\n")
                    .chunked(1000)
                    .map { it.joinToString(separator = "\n") }
                    .forEach {
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = it,
                            fontFamily = fontFamily,
                            fontStyle = fontStyle,
                            fontSize = fontSize,
                            lineHeight = if (fontSize.isUnspecified) TextUnit.Unspecified else fontSize * 1.2f,
                        )
                    }
            }
        }
    }
}

@Composable
private fun SyntaxHighlightedText(text: String, language: String, fontSize: TextUnit) {
    val limitedText = text.truncate(120_000)
    var wrapLines by rememberSaveable(key = "wrap-lines") {
        mutableStateOf(true)
    }
    val scrollState = rememberScrollState()

    val syntaxHighlighter = rememberSyntaxHighlighter(language)
    var formattedText by remember {
        mutableStateOf(AnnotatedString(limitedText))
    }
    val coroutineScope = rememberCoroutineScope()
    DisposableEffect(syntaxHighlighter, limitedText) {
        val job = coroutineScope.launch(Dispatchers.Default) {
            formattedText = buildAnnotatedString {
                append(limitedText)
                syntaxHighlighter.applyFormatting(this, limitedText)
            }
        }
        onDispose {
            job.cancel()
        }
    }

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
                text = formattedText,
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = fontSize,
                    lineHeight = if (fontSize.isUnspecified) TextUnit.Unspecified else fontSize * 1.2f,
                ),
            )
        }
    }
}

@Composable
private fun TableView(tableData: TableData, fontSize: TextUnit) {
    SelectionContainer {
        Table(
            modifier = Modifier
                .padding(4.dp)
                .fillMaxWidth(),
            rowCount = tableData.rows.size + 1,
            columnCount = tableData.columns.size,
            maxCellWidthDp = 300.dp,
            stickyRowCount = 1,
        ) { rowIndex, columnIndex ->
            if (rowIndex == 0) {
                Text(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.background)
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    text = tableData.columns[columnIndex],
                    fontWeight = FontWeight.SemiBold,
                    fontSize = fontSize,
                    lineHeight = if (fontSize.isUnspecified) TextUnit.Unspecified else fontSize * 1.2f,
                )
            } else {
                Text(
                    modifier = Modifier
                        .runIf(rowIndex % 2 == 0) {
                            background(Color(0x10909090))
                        }
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    text = tableData.rows[rowIndex - 1][tableData.columns[columnIndex]] ?: "",
                    fontSize = fontSize,
                    lineHeight = if (fontSize.isUnspecified) TextUnit.Unspecified else fontSize * 1.2f,
                )
            }
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
                        .semantics(mergeDescendants = true) {}
                        .toggleable(
                            value = permanentlyHidden,
                            onValueChange = {
                                permanentlyHidden = !permanentlyHidden
                                onDoNotShowAgain(permanentlyHidden)
                            },
                        )
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
        },
    )
}

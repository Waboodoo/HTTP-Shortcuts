package ch.rmy.android.http_shortcuts.activities.editor.body

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.editor.body.models.ParameterListItem
import ch.rmy.android.http_shortcuts.components.Checkbox
import ch.rmy.android.http_shortcuts.components.EmptyState
import ch.rmy.android.http_shortcuts.components.FontSize
import ch.rmy.android.http_shortcuts.components.SelectionField
import ch.rmy.android.http_shortcuts.components.Spacing
import ch.rmy.android.http_shortcuts.components.SuggestionDropdown
import ch.rmy.android.http_shortcuts.components.VariablePlaceholderText
import ch.rmy.android.http_shortcuts.components.VariablePlaceholderTextField
import ch.rmy.android.http_shortcuts.data.enums.ParameterType
import ch.rmy.android.http_shortcuts.data.enums.RequestBodyType
import ch.rmy.android.http_shortcuts.extensions.rememberSyntaxHighlighter
import ch.rmy.android.http_shortcuts.utils.FileTypeUtil
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

@Composable
fun RequestBodyContent(
    requestBodyType: RequestBodyType,
    parameters: List<ParameterListItem>,
    contentType: String,
    bodyContent: String,
    bodyContentError: String,
    syntaxHighlightingLanguage: String?,
    useImageEditor: Boolean,
    onRequestBodyTypeChanged: (RequestBodyType) -> Unit,
    onContentTypeChanged: (String) -> Unit,
    onBodyContentChanged: (String) -> Unit,
    onFormatButtonClicked: () -> Unit,
    onParameterClicked: (String) -> Unit,
    onParameterMoved: (String, String) -> Unit,
    onUseImageEditorChanged: (Boolean) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = Spacing.MEDIUM),
        verticalArrangement = Arrangement.spacedBy(Spacing.MEDIUM),
    ) {
        SelectionField(
            modifier = Modifier.padding(horizontal = Spacing.MEDIUM),
            title = stringResource(R.string.label_request_body_type),
            selectedKey = requestBodyType,
            items = listOf(
                RequestBodyType.CUSTOM_TEXT to stringResource(R.string.request_body_option_custom_text),
                RequestBodyType.FORM_DATA to stringResource(R.string.request_body_option_form_data),
                RequestBodyType.X_WWW_FORM_URLENCODE to stringResource(R.string.request_body_option_x_www_form_urlencoded),
                RequestBodyType.FILE to stringResource(R.string.request_body_option_file),
                RequestBodyType.CAMERA_IMAGE to stringResource(R.string.request_body_option_image),
            ),
            onItemSelected = onRequestBodyTypeChanged,
        )

        when (requestBodyType) {
            RequestBodyType.CUSTOM_TEXT -> {
                BodyTextEditor(
                    contentType = contentType,
                    bodyContent = bodyContent,
                    bodyContentError = bodyContentError,
                    syntaxHighlightingLanguage = syntaxHighlightingLanguage,
                    onContentTypeChanged = onContentTypeChanged,
                    onBodyContentChanged = onBodyContentChanged,
                    onFormatButtonClicked = onFormatButtonClicked,
                )
            }
            RequestBodyType.FORM_DATA,
            RequestBodyType.X_WWW_FORM_URLENCODE,
            -> {
                ParameterList(
                    parameters = parameters,
                    onParameterClicked = onParameterClicked,
                    onParameterMoved = onParameterMoved,
                )
            }
            RequestBodyType.FILE,
            RequestBodyType.CAMERA_IMAGE,
            -> {
                FileOptions(
                    useImageEditor = useImageEditor,
                    onUseImageEditorChanged = onUseImageEditorChanged,
                )
            }
        }
    }
}

@Composable
private fun ColumnScope.BodyTextEditor(
    contentType: String,
    bodyContent: String,
    bodyContentError: String,
    syntaxHighlightingLanguage: String?,
    onContentTypeChanged: (String) -> Unit,
    onBodyContentChanged: (String) -> Unit,
    onFormatButtonClicked: () -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(horizontal = Spacing.MEDIUM)
            .weight(1f),
        verticalArrangement = Arrangement.spacedBy(Spacing.SMALL),
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            var hasFocus by remember {
                mutableStateOf(false)
            }
            TextField(
                modifier = Modifier
                    .onFocusChanged {
                        hasFocus = it.isFocused
                    }
                    .fillMaxWidth(),
                value = contentType,
                label = {
                    Text(stringResource(R.string.label_content_type))
                },
                onValueChange = onContentTypeChanged,
                textStyle = TextStyle(
                    fontSize = FontSize.SMALL,
                ),
                singleLine = true,
            )

            SuggestionDropdown(
                prompt = contentType,
                isActive = hasFocus,
                options = CONTENT_TYPE_SUGGESTIONS,
                onSuggestionSelected = onContentTypeChanged,
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(Spacing.TINY),
            modifier = Modifier.weight(1f),
        ) {
            val syntaxHighlighter = syntaxHighlightingLanguage?.let {
                rememberSyntaxHighlighter(it)
            }
            VariablePlaceholderTextField(
                modifier = Modifier.weight(1f),
                key = "body-content",
                value = bodyContent,
                minLines = 10,
                label = {
                    Text(stringResource(R.string.label_custom_body))
                },
                placeholder = {
                    Text(stringResource(R.string.placeholder_request_body_content))
                },
                onValueChange = onBodyContentChanged,
                textStyle = TextStyle.Default.copy(
                    fontFamily = FontFamily.Monospace,
                ),
                transformation = {
                    syntaxHighlighter?.applyFormatting(this, it)
                },
                isError = bodyContentError.isNotEmpty(),
                supportingText = bodyContentError.takeUnlessEmpty()?.let {
                    {
                        Text(it)
                    }
                }
            )

            AnimatedVisibility(visible = contentType == FileTypeUtil.TYPE_JSON) {
                Button(
                    onClick = onFormatButtonClicked,
                ) {
                    Text(stringResource(R.string.button_format_json))
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.ParameterList(
    parameters: List<ParameterListItem>,
    onParameterClicked: (String) -> Unit,
    onParameterMoved: (String, String) -> Unit,
) {
    if (parameters.isEmpty()) {
        EmptyState(
            title = stringResource(R.string.empty_state_request_parameters),
            description = stringResource(R.string.empty_state_request_parameters_instructions),
        )
        return
    }

    val reorderableState = rememberReorderableLazyListState(
        onMove = { from, to ->
            onParameterMoved(from.key as String, to.key as String)
        },
    )

    LazyColumn(
        state = reorderableState.listState,
        modifier = Modifier
            .weight(1f)
            .reorderable(reorderableState)
            .detectReorderAfterLongPress(reorderableState),
    ) {
        items(
            items = parameters,
            key = { it.id },
        ) { item ->
            ReorderableItem(reorderableState, key = item.id) { isDragging ->
                val elevation = animateDpAsState(if (isDragging) 16.dp else 0.dp)
                ParameterItem(
                    parameter = item,
                    modifier = Modifier
                        .shadow(elevation.value)
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable {
                            onParameterClicked(item.id)
                        },
                )
            }
        }
    }
}

@Composable
private fun ParameterItem(
    parameter: ParameterListItem,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier,
    ) {
        ListItem(
            modifier = Modifier.fillMaxWidth(),
            headlineContent = {
                VariablePlaceholderText(parameter.key, maxLines = 2, overflow = TextOverflow.Ellipsis)
            },
            supportingContent = {
                VariablePlaceholderText(
                    text = when (parameter.type) {
                        ParameterType.FILE -> stringResource(R.string.subtitle_parameter_value_file)
                        ParameterType.FILES -> stringResource(R.string.subtitle_parameter_value_files)
                        ParameterType.IMAGE -> stringResource(R.string.subtitle_parameter_value_image)
                        ParameterType.STRING -> parameter.value.ifEmpty { stringResource(R.string.empty_option_placeholder) }
                    },
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            },
        )
        Divider()
    }
}

@Composable
private fun FileOptions(
    useImageEditor: Boolean,
    onUseImageEditorChanged: (Boolean) -> Unit,
) {
    Checkbox(
        label = stringResource(R.string.label_file_upload_options_allow_image_editing),
        checked = useImageEditor,
        onCheckedChange = onUseImageEditorChanged,
    )
}

private val CONTENT_TYPE_SUGGESTIONS = arrayOf(
    "application/javascript",
    "application/json",
    "application/octet-stream",
    "application/xml",
    "text/css",
    "text/csv",
    "text/plain",
    "text/html",
    "text/xml",
)

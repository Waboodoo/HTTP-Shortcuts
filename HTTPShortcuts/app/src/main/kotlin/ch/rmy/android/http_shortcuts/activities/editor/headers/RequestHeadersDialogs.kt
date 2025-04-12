package ch.rmy.android.http_shortcuts.activities.editor.headers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.lifecycle.SavedStateHandle
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.FontSize
import ch.rmy.android.http_shortcuts.components.Spacing
import ch.rmy.android.http_shortcuts.components.SuggestionDropdown
import ch.rmy.android.http_shortcuts.components.VariablePlaceholderTextField
import ch.rmy.android.http_shortcuts.utils.Validation

@Composable
fun RequestHeadersDialogs(
    dialogState: RequestHeadersDialogState?,
    savedStateHandle: SavedStateHandle,
    onConfirmed: (key: String, value: String) -> Unit,
    onDelete: () -> Unit,
    onDismissed: () -> Unit,
) {
    when (dialogState) {
        is RequestHeadersDialogState.AddHeader -> {
            EditHeaderDialog(
                savedStateHandle = savedStateHandle,
                isEdit = false,
                onConfirmed = onConfirmed,
                onDismissed = onDismissed,
            )
        }
        is RequestHeadersDialogState.EditHeader -> {
            EditHeaderDialog(
                savedStateHandle = savedStateHandle,
                isEdit = true,
                initialKey = dialogState.key,
                initialValue = dialogState.value,
                onConfirmed = onConfirmed,
                onDelete = onDelete,
                onDismissed = onDismissed,
            )
        }
        null -> Unit
    }
}

@Composable
private fun EditHeaderDialog(
    savedStateHandle: SavedStateHandle,
    isEdit: Boolean,
    initialKey: String = "",
    initialValue: String = "",
    onConfirmed: (key: String, value: String) -> Unit,
    onDelete: () -> Unit = {},
    onDismissed: () -> Unit,
) {
    var key by rememberSaveable(key = "edit-header-key") {
        mutableStateOf(initialKey)
    }
    var value by rememberSaveable(key = "edit-header-value") {
        mutableStateOf(initialValue)
    }

    val invalidCharacterInKey = findInvalidCharacterInKey(key)
    val invalidCharacterInValue = findInvalidCharacterInValue(value)

    AlertDialog(
        onDismissRequest = onDismissed,
        title = {
            Text(stringResource(if (isEdit) R.string.title_custom_header_edit else R.string.title_custom_header_add))
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(Spacing.SMALL),
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    var hasFocus by remember {
                        mutableStateOf(false)
                    }
                    VariablePlaceholderTextField(
                        savedStateHandle = savedStateHandle,
                        modifier = Modifier
                            .onFocusChanged {
                                hasFocus = it.isFocused
                            }
                            .fillMaxWidth(),
                        key = "header-edit-key",
                        value = key,
                        label = {
                            Text(stringResource(R.string.label_custom_header_key))
                        },
                        onValueChange = {
                            key = it
                        },
                        textStyle = TextStyle(
                            fontSize = FontSize.SMALL,
                        ),
                        maxLines = 4,
                        isError = invalidCharacterInKey != null,
                        supportingText = invalidCharacterInKey?.let {
                            {
                                Text(stringResource(R.string.error_invalid_character, it))
                            }
                        },
                    )

                    SuggestionDropdown(
                        prompt = key,
                        isActive = hasFocus,
                        options = SUGGESTED_KEYS,
                        onSuggestionSelected = {
                            key = it
                        },
                    )
                }

                VariablePlaceholderTextField(
                    savedStateHandle = savedStateHandle,
                    modifier = Modifier
                        .fillMaxWidth(),
                    key = "header-edit-value",
                    value = value,
                    label = {
                        Text(stringResource(R.string.label_custom_header_value))
                    },
                    onValueChange = {
                        value = it
                    },
                    textStyle = TextStyle(
                        fontSize = FontSize.SMALL,
                    ),
                    maxLines = 4,
                    isError = invalidCharacterInValue != null,
                    supportingText = invalidCharacterInValue?.let {
                        {
                            Text(stringResource(R.string.error_invalid_character, it))
                        }
                    },
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = key.isNotEmpty() && invalidCharacterInKey == null && invalidCharacterInValue == null,
                onClick = {
                    onConfirmed(key, value)
                },
            ) {
                Text(stringResource(R.string.dialog_ok))
            }
        },
        dismissButton = {
            if (isEdit) {
                TextButton(
                    onClick = onDelete,
                ) {
                    Text(stringResource(R.string.dialog_remove))
                }
            }
        },
    )
}

private fun findInvalidCharacterInKey(key: String): Char? =
    key.firstOrNull { c ->
        !Validation.isValidInHeaderName(c)
    }

private fun findInvalidCharacterInValue(value: String): Char? =
    value.firstOrNull { c ->
        !Validation.isValidInHeaderValue(c)
    }

private val SUGGESTED_KEYS = arrayOf(
    "Accept",
    "Accept-Charset",
    "Accept-Encoding",
    "Accept-Language",
    "Accept-Datetime",
    "Authorization",
    "Cache-Control",
    "Connection",
    "Cookie",
    "Content-Length",
    "Content-MD5",
    "Content-Type",
    "Date",
    "Expect",
    "Forwarded",
    "From",
    "Host",
    "If-Match",
    "If-Modified-Since",
    "If-None-Match",
    "If-Range",
    "If-Unmodified-Since",
    "Max-Forwards",
    "Origin",
    "Pragma",
    "Proxy-Authorization",
    "Range",
    "Referer",
    "User-Agent",
    "Upgrade",
    "Via",
    "Warning",
)

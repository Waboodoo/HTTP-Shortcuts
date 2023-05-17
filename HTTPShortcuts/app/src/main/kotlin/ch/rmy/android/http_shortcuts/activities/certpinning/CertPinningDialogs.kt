package ch.rmy.android.http_shortcuts.activities.certpinning

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.ConfirmDialog
import ch.rmy.android.http_shortcuts.components.FontSize
import ch.rmy.android.http_shortcuts.components.SelectDialog
import ch.rmy.android.http_shortcuts.components.SelectDialogEntry
import ch.rmy.android.http_shortcuts.components.Spacing

@Composable
fun CertPinningDialogs(
    dialogState: CertPinningDialogState?,
    onEditConfirmed: (pattern: String, hash: String) -> Unit,
    onEditOptionSelected: () -> Unit,
    onDeleteOptionSelected: () -> Unit,
    onDeletionConfirmed: () -> Unit,
    onDismissed: () -> Unit,
) {
    when (dialogState) {
        is CertPinningDialogState.ContextMenu -> ContextMenuDialog(
            onEditOptionSelected,
            onDeleteOptionSelected,
            onDismissed,
        )
        is CertPinningDialogState.ConfirmDeletion -> ConfirmDeletionDialog(
            onDeletionConfirmed,
            onDismissed,
        )
        is CertPinningDialogState.Editor -> EditorDialog(
            dialogState.initialPattern,
            dialogState.initialHash,
            onEditConfirmed,
            onDismissed,
        )

        null -> Unit
    }
}

@Composable
private fun ContextMenuDialog(
    onEditOptionSelected: () -> Unit,
    onDeleteOptionSelected: () -> Unit,
    onDismissed: () -> Unit,
) {
    SelectDialog(
        onDismissRequest = onDismissed,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            SelectDialogEntry(
                label = stringResource(R.string.action_edit),
                onClick = onEditOptionSelected,
            )
            SelectDialogEntry(
                label = stringResource(R.string.action_delete),
                onClick = onDeleteOptionSelected,
            )
        }
    }
}

@Composable
private fun ConfirmDeletionDialog(
    onConfirmed: () -> Unit,
    onDismissed: () -> Unit,
) {
    ConfirmDialog(
        message = stringResource(R.string.confirm_delete_certificate_pinning_message),
        confirmButton = stringResource(R.string.dialog_delete),
        onConfirmRequest = onConfirmed,
        onDismissRequest = onDismissed,
    )
}

@Composable
private fun EditorDialog(
    initialPattern: String,
    initialHash: String,
    onEditConfirmed: (pattern: String, hash: String) -> Unit,
    onDismissed: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    var patternValue by rememberSaveable(key = "pattern") {
        mutableStateOf(initialPattern)
    }
    var hashValue by rememberSaveable(key = "hash") {
        mutableStateOf(sanitizeHash(initialHash))
    }

    val confirmButtonEnabled by remember {
        derivedStateOf {
            patternValue.isValidPattern() && hashValue.isValidHash()
        }
    }
    AlertDialog(
        onDismissRequest = onDismissed,
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(Spacing.SMALL)
            ) {
                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    label = {
                        Text(stringResource(R.string.label_certificate_pinning_hostname_pattern))
                    },
                    placeholder = {
                        Text(stringResource(R.string.hint_certificate_pinning_hostname_pattern))
                    },
                    value = patternValue,
                    onValueChange = {
                        patternValue = sanitizePattern(it)
                    },
                    singleLine = false,
                    textStyle = TextStyle(
                        fontSize = FontSize.SMALL,
                        fontFamily = FontFamily.Monospace,
                    ),
                )

                TextField(
                    modifier = Modifier
                        .fillMaxWidth(),
                    label = {
                        Text(stringResource(R.string.label_certificate_pinning_fingerprint))
                    },
                    placeholder = {
                        Text(stringResource(R.string.hint_certificate_pinning_fingerprint))
                    },
                    value = hashValue,
                    onValueChange = {
                        hashValue = sanitizeHash(it)
                    },
                    singleLine = false,
                    textStyle = TextStyle(
                        fontSize = FontSize.SMALL,
                        fontFamily = FontFamily.Monospace,
                    ),
                    minLines = 5,
                    visualTransformation = {
                        TransformedText(
                            AnnotatedString(transformHash(it.text)),
                            HashOffsetMapping,
                        )
                    },
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = confirmButtonEnabled,
                onClick = {
                    onEditConfirmed(patternValue, hashValue)
                },
            ) {
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

private val PATTERN_REGEX = """^(\*{1,2}\.)?([A-Za-z0-9_\-]+\.)*[A-Za-z0-9_\-]+$""".toRegex()
private val UNSUPPORTED_HASH_SYMBOLS_REGEX = "[^A-F0-9]".toRegex()
private val UNSUPPORTED_PATTERN_SYMBOLS_REGEX = "[\\s,;]".toRegex()

private fun String.isValidPattern(): Boolean =
    matches(PATTERN_REGEX)

private fun String.isValidHash(): Boolean =
    length == 40 || length == 64

private fun sanitizePattern(input: String): String =
    input.lowercase()
        .replace(UNSUPPORTED_PATTERN_SYMBOLS_REGEX, "")

private fun sanitizeHash(input: String): String =
    input.uppercase()
        .replace(UNSUPPORTED_HASH_SYMBOLS_REGEX, "")
        .take(64)

private fun transformHash(input: String): String =
    input.chunked(2).joinToString(separator = ":")

private object HashOffsetMapping : OffsetMapping {
    override fun originalToTransformed(offset: Int): Int =
        when (offset) {
            0 -> 0
            else -> offset + (offset + 1) / 2 - 1
        }

    override fun transformedToOriginal(offset: Int): Int =
        offset - offset / 3
}

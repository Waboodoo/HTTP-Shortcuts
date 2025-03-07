package ch.rmy.android.http_shortcuts.activities.editor.scripting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import ch.rmy.android.framework.extensions.consume
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.editor.scripting.models.CodeFieldType
import ch.rmy.android.http_shortcuts.components.CodeEditorField
import ch.rmy.android.http_shortcuts.components.EventHandler
import ch.rmy.android.http_shortcuts.components.Spacing
import ch.rmy.android.http_shortcuts.data.enums.ShortcutExecutionType
import ch.rmy.android.http_shortcuts.extensions.insertAtCursor
import ch.rmy.android.http_shortcuts.extensions.runIf
import ch.rmy.android.http_shortcuts.utils.SyntaxHighlighter
import kotlinx.coroutines.delay

@Composable
fun ScriptingContent(
    activeFieldType: CodeFieldType,
    codeOnPrepare: String,
    codeOnSuccess: String,
    codeOnFailure: String,
    shortcutExecutionType: ShortcutExecutionType,
    onCodeOnPrepareChanged: (String) -> Unit,
    onCodeOnSuccessChanged: (String) -> Unit,
    onCodeOnFailureChanged: (String) -> Unit,
    onActiveFieldChanged: (CodeFieldType) -> Unit,
) {
    val usesMultipleInputFields = shortcutExecutionType == ShortcutExecutionType.HTTP
    Column(
        modifier = Modifier.runIf(usesMultipleInputFields) {
            verticalScroll(rememberScrollState())
        },
        verticalArrangement = Arrangement.spacedBy(Spacing.SMALL),
    ) {
        CodeSection(
            modifier = Modifier
                .runIf(!usesMultipleInputFields) {
                    fillMaxSize()
                },
            isFocused = activeFieldType == CodeFieldType.PREPARE,
            autoFocus = codeOnPrepare.isEmpty() && codeOnSuccess.isEmpty() && codeOnFailure.isEmpty(),
            code = codeOnPrepare,
            placeholder = stringResource(
                if (shortcutExecutionType == ShortcutExecutionType.SCRIPTING) {
                    R.string.placeholder_javascript_code_generic
                } else {
                    R.string.placeholder_javascript_code_before
                },
            ),
            label = if (usesMultipleInputFields) {
                stringResource(R.string.label_pre_request_script)
            } else {
                null
            },
            minLines = if (usesMultipleInputFields) 6 else 12,
            onCodeChanged = onCodeOnPrepareChanged,
            onFocused = {
                onActiveFieldChanged(CodeFieldType.PREPARE)
            },
        )

        if (usesMultipleInputFields) {
            CodeSection(
                isFocused = activeFieldType == CodeFieldType.SUCCESS,
                code = codeOnSuccess,
                label = stringResource(R.string.label_post_request_success_script),
                placeholder = stringResource(R.string.placeholder_javascript_code_success),
                onCodeChanged = onCodeOnSuccessChanged,
                onFocused = {
                    onActiveFieldChanged(CodeFieldType.SUCCESS)
                },
            )

            CodeSection(
                isFocused = activeFieldType == CodeFieldType.FAILURE,
                code = codeOnFailure,
                label = stringResource(R.string.label_post_request_failure_script),
                placeholder = stringResource(R.string.placeholder_javascript_code_failure),
                onCodeChanged = onCodeOnFailureChanged,
                onFocused = {
                    onActiveFieldChanged(CodeFieldType.FAILURE)
                },
            )
        }
    }
}

@Composable
private fun CodeSection(
    isFocused: Boolean,
    code: String,
    label: String?,
    placeholder: String,
    onFocused: () -> Unit,
    onCodeChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
    autoFocus: Boolean = false,
    minLines: Int = 6,
) {
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current

    var selectionStart by rememberSaveable {
        mutableIntStateOf(code.length)
    }
    var selectionEnd by rememberSaveable {
        mutableIntStateOf(code.length)
    }

    var textFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = code,
                selection = TextRange(selectionStart, selectionEnd),
            ),
        )
    }

    DisposableEffect(code) {
        if (code != textFieldValue.text) {
            textFieldValue = textFieldValue.copy(
                text = code,
            )
        }
        onDispose { }
    }

    if (isFocused) {
        EventHandler { event ->
            when (event) {
                is ScriptingEvent.InsertCodeSnippet -> consume {
                    textFieldValue = textFieldValue.insertAtCursor(event.textBeforeCursor, event.textAfterCursor)
                    onCodeChanged(textFieldValue.text)
                    focusRequester.requestFocus()
                    keyboard?.show()
                }
                else -> false
            }
        }
    }

    CodeEditorField(
        modifier = Modifier
            .padding(Spacing.SMALL)
            .fillMaxWidth()
            .onFocusChanged {
                if (it.isFocused) {
                    onFocused()
                }
            }
            .focusRequester(focusRequester)
            .then(modifier),
        language = SyntaxHighlighter.Languages.JS,
        label = label,
        value = textFieldValue,
        minLines = minLines,
        onValueChange = {
            selectionStart = it.selection.start
            selectionEnd = it.selection.end
            val hasChanged = textFieldValue.text != it.text
            textFieldValue = it
            if (hasChanged) {
                onCodeChanged(it.text)
            }
        },
        placeholder = placeholder,
    )

    LaunchedEffect(Unit) {
        if (autoFocus) {
            delay(50)
            focusRequester.requestFocus()
            keyboard?.show()
        }
    }
}

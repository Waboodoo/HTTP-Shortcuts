package ch.rmy.android.http_shortcuts.activities.curl_import

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import ch.rmy.android.framework.extensions.truncate
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.FontSize
import ch.rmy.android.http_shortcuts.components.HelpText
import ch.rmy.android.http_shortcuts.components.Spacing
import ch.rmy.android.http_shortcuts.utils.rememberSyntaxHighlighter
import ch.rmy.android.http_shortcuts.utils.syntaxHighlightingVisualTransformation

@Composable
fun CurlImportContent(
    inputText: String,
    unsupportedOptions: List<String>,
    onInputTextChanged: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    val syntaxHighlighter = rememberSyntaxHighlighter("sh")
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    var focusRequested by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier
            .padding(Spacing.MEDIUM)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.MEDIUM),
    ) {
        HelpText(stringResource(R.string.instructions_curl_import))

        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .onGloballyPositioned {
                    if (!focusRequested) {
                        focusRequested = true
                        focusRequester.requestFocus()
                        keyboard?.show()
                    }
                }
                .weight(1f)
                .clearAndSetSemantics { },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                autoCorrectEnabled = false,
                imeAction = ImeAction.Go,
            ),
            keyboardActions = KeyboardActions {
                onSubmit()
            },
            textStyle = TextStyle(
                fontFamily = FontFamily.Monospace,
            ),
            value = inputText,
            onValueChange = onInputTextChanged,
            visualTransformation = syntaxHighlightingVisualTransformation(syntaxHighlighter, inputText),
        )

        if (unsupportedOptions.isNotEmpty()) {
            Text(
                modifier = Modifier.padding(top = Spacing.TINY),
                text = buildAnnotatedString {
                    append(stringResource(R.string.warning_unsupported_curl_options))
                    append(" ")
                    pushStyle(SpanStyle(fontFamily = FontFamily.Monospace))
                    append(unsupportedOptions.joinToString(", ").truncate(100))
                },
                color = colorResource(R.color.warning),
                fontSize = FontSize.SMALL,
            )
        }
    }
}

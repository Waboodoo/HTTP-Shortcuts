package ch.rmy.android.http_shortcuts.activities.curl_import

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.HelpText
import ch.rmy.android.http_shortcuts.components.Spacing
import ch.rmy.android.http_shortcuts.extensions.rememberSyntaxHighlighter

@Composable
fun CurlImportContent(
    inputText: String,
    onInputTextChanged: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    val syntaxHighlighter = rememberSyntaxHighlighter("sh")
    val focusRequester = remember { FocusRequester() }

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
                    focusRequester.requestFocus()
                }
                .weight(1f),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                autoCorrect = false,
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
            visualTransformation = {
                TransformedText(syntaxHighlighter.format(it.text), OffsetMapping.Identity)
            },
        )
    }
}

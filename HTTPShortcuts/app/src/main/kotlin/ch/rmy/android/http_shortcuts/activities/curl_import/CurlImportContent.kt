package ch.rmy.android.http_shortcuts.activities.curl_import

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.FontSize
import ch.rmy.android.http_shortcuts.components.Spacing

@Composable
fun CurlImportContent(
    inputText: String,
    onInputTextChanged: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }

    Column(
        modifier = Modifier
            .padding(Spacing.MEDIUM)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.MEDIUM),
    ) {
        Text(
            text = stringResource(R.string.instructions_curl_import),
            fontSize = FontSize.MEDIUM,
        )

        Box(
            modifier = Modifier
                .background(colorResource(R.color.textarea_background))
                .fillMaxWidth()
        ) {
            BasicTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .onGloballyPositioned {
                        focusRequester.requestFocus()
                    }
                    .padding(Spacing.SMALL)
                    .heightIn(min = 200.dp),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Go,
                ),
                keyboardActions = KeyboardActions {
                    onSubmit()
                },
                textStyle = TextStyle(
                    color = colorResource(R.color.text_color_primary_dark),
                    fontFamily = FontFamily.Monospace,
                ),
                value = inputText,
                onValueChange = onInputTextChanged
            )
        }
    }
}

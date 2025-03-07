package ch.rmy.android.http_shortcuts.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.semantics.editableText
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.utils.rememberSyntaxHighlighter
import ch.rmy.android.http_shortcuts.utils.syntaxHighlightingVisualTransformation

@Composable
fun CodeEditorField(
    language: String,
    value: TextFieldValue,
    placeholder: String,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    minLines: Int = 6,
    label: String? = null,
) {
    val syntaxHighlighter = rememberSyntaxHighlighter(language)
    val textFieldColors = if (isSystemInDarkTheme()) {
        val backgroundColor = colorResource(R.color.textarea_background)
        TextFieldDefaults.colors(
            focusedContainerColor = backgroundColor,
            unfocusedContainerColor = backgroundColor,
            disabledContainerColor = backgroundColor,
        )
    } else {
        TextFieldDefaults.colors()
    }

    TextField(
        modifier = modifier.semantics {
            editableText = AnnotatedString(value.text)
        },
        colors = textFieldColors,
        value = value,
        onValueChange = onValueChange,
        textStyle = TextStyle(
            fontFamily = FontFamily.Monospace,
        ),
        placeholder = {
            Text(text = placeholder)
        },
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.None,
            autoCorrectEnabled = false,
        ),
        visualTransformation = syntaxHighlightingVisualTransformation(syntaxHighlighter, value.text),
        minLines = minLines,
        label = if (label != null) {
            {
                Text(label)
            }
        } else {
            null
        },
    )
}

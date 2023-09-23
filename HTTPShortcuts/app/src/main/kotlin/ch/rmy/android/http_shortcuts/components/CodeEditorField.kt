package ch.rmy.android.http_shortcuts.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.extensions.rememberSyntaxHighlighter

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

    var previousText by remember {
        mutableStateOf("")
    }
    var previousAnnotated by remember {
        mutableStateOf(AnnotatedString(""))
    }

    TextField(
        modifier = modifier,
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
            autoCorrect = false,
        ),
        visualTransformation = {
            val formatted = if (it.text == previousText) {
                previousAnnotated
            } else {
                syntaxHighlighter.format(it.text)
                    .apply {
                        previousText = it.text
                        previousAnnotated = this
                    }
            }
            TransformedText(formatted, OffsetMapping.Identity)
        },
        minLines = minLines,
        label = if (label != null) {
            {
                Text(label)
            }
        } else null,
    )
}

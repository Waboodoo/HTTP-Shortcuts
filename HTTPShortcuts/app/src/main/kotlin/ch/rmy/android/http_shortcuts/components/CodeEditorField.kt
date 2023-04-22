package ch.rmy.android.http_shortcuts.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import ch.rmy.android.http_shortcuts.utils.SyntaxHighlighter
import com.wakaztahir.codeeditor.highlight.theme.DefaultTheme

@Composable
fun CodeEditorField(
    language: String,
    value: TextFieldValue,
    placeholder: String,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
) {
    val theme = remember {
        // TODO: Change with dark mode
        DefaultTheme()
    }
    val syntaxHighlighter = remember(language, theme) {
        SyntaxHighlighter(language, theme)
    }

    TextField(
        modifier = Modifier
            .padding(Spacing.MEDIUM)
            .fillMaxSize()
            .then(modifier),
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
            TransformedText(syntaxHighlighter.format(it.text), OffsetMapping.Identity)
        },
    )
}

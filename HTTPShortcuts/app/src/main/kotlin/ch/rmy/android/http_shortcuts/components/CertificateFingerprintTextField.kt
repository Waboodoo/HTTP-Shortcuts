package ch.rmy.android.http_shortcuts.components

import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import ch.rmy.android.http_shortcuts.extensions.isValidCertificateFingerprint

@Composable
fun CertificateFingerprintTextField(
    value: String,
    label: String,
    placeholder: String,
    modifier: Modifier,
    enabled: Boolean = true,
    onValueChanged: (String) -> Unit,
) {
    TextField(
        modifier = modifier,
        enabled = enabled,
        label = {
            Text(label)
        },
        placeholder = {
            Text(placeholder)
        },
        value = value,
        onValueChange = {
            onValueChanged(sanitizeHash(it))
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
        isError = value.isNotEmpty() && !value.isValidCertificateFingerprint(),
    )
}

private val UNSUPPORTED_HASH_SYMBOLS_REGEX = "[^A-F0-9]".toRegex()

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

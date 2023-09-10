package ch.rmy.android.http_shortcuts.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.rmy.android.http_shortcuts.R

@Composable
fun VariablePlaceholderText(
    text: String,
    modifier: Modifier = Modifier,
    overflow: TextOverflow = TextOverflow.Clip,
    maxLines: Int = Int.MAX_VALUE,
) {
    val viewModel = hiltViewModel<VariablePlaceholderViewModel>()
    val placeholders by viewModel.variablePlaceholders.collectAsStateWithLifecycle()
    val placeholderColor = colorResource(R.color.variable)
    val placeholderStyle = remember {
        SpanStyle(
            color = placeholderColor,
            fontFamily = FontFamily.Monospace,
        )
    }
    val transformedText = remember(text, placeholders) {
        var offsetSum = 0
        val ranges = mutableListOf<IntRange>()
        val transformedText = VARIABLE_PLACEHOLDER_REGEX.replace(text) { result ->
            val (variableId) = result.destructured
            val placeholder = placeholders.find { it.variableId == variableId } ?: return@replace result.value
            val variableKey = placeholder.variableKey
            val replacement = "{$variableKey}"
            val lengthDiff = replacement.length - result.value.length
            ranges.add(IntRange(result.range.first + offsetSum, result.range.last + lengthDiff + offsetSum))
            offsetSum += lengthDiff
            replacement
        }
        buildAnnotatedString {
            append(transformedText)
            ranges.forEach { range ->
                addStyle(placeholderStyle, range.first, range.last + 1)
            }
        }
    }

    Text(
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier),
        text = transformedText,
        overflow = overflow,
        maxLines = maxLines,
    )
}

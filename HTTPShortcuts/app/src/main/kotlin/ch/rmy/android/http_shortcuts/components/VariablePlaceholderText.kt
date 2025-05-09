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

private data class Range(
    val isLocalVariable: Boolean,
    val range: IntRange,
)

@Composable
fun VariablePlaceholderText(
    text: String,
    modifier: Modifier = Modifier,
    overflow: TextOverflow = TextOverflow.Clip,
    maxLines: Int = Int.MAX_VALUE,
) {
    val viewModel = hiltViewModel<VariablePlaceholderViewModel>()
    val placeholders by viewModel.variablePlaceholders.collectAsStateWithLifecycle()
    val globalVariablePlaceholderColor = colorResource(R.color.global_variable)
    val globalVariablePlaceholderStyle = remember(globalVariablePlaceholderColor) {
        SpanStyle(
            color = globalVariablePlaceholderColor,
            fontFamily = FontFamily.Monospace,
        )
    }
    val localVariablePlaceholderColor = colorResource(R.color.local_variable)
    val localVariablePlaceholderStyle = remember(localVariablePlaceholderColor) {
        SpanStyle(
            color = localVariablePlaceholderColor,
            fontFamily = FontFamily.Monospace,
        )
    }
    val transformedText = remember(text, placeholders) {
        var offsetSum = 0
        val ranges = mutableListOf<Range>()
        val transformedText = VARIABLE_PLACEHOLDER_REGEX.replace(text) { result ->
            val (variableKeyOrId) = result.destructured
            val placeholder = placeholders.find { it.globalVariableId == variableKeyOrId || it.variableKey == variableKeyOrId }
            val variableKey = placeholder?.variableKey
            val replacement = if (placeholder != null && placeholder.globalVariableId == variableKeyOrId) {
                "{$variableKey}"
            } else {
                result.value
            }
            val lengthDiff = replacement.length - result.value.length
            ranges.add(
                Range(
                    isLocalVariable = placeholder == null,
                    range = IntRange(result.range.first + offsetSum, result.range.last + lengthDiff + offsetSum),
                ),
            )
            offsetSum += lengthDiff
            replacement
        }
        buildAnnotatedString {
            append(transformedText)
            ranges.forEach { (isLocalVariable, range) ->
                val style = if (isLocalVariable) {
                    localVariablePlaceholderStyle
                } else {
                    globalVariablePlaceholderStyle
                }
                addStyle(style, range.first, range.last + 1)
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

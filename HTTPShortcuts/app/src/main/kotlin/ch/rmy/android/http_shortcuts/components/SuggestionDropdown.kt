package ch.rmy.android.http_shortcuts.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.PopupProperties

@Composable
fun SuggestionDropdown(
    options: Array<String>,
    prompt: String,
    isActive: Boolean,
    onSuggestionSelected: (String) -> Unit,
) {
    var suggestions by remember {
        mutableStateOf(emptyList<String>())
    }
    LaunchedEffect(prompt, isActive, options) {
        if (isActive && prompt.length >= 2) {
            suggestions = options.filter {
                isMatch(it, prompt)
            }
        } else if (suggestions.isNotEmpty()) {
            suggestions = emptyList()
        }
    }

    DropdownMenu(
        expanded = suggestions.isNotEmpty(),
        onDismissRequest = { suggestions = emptyList() },
        properties = PopupProperties(focusable = false),
    ) {
        suggestions.forEach { suggestion ->
            DropdownMenuItem(
                onClick = {
                    onSuggestionSelected(suggestion)
                    suggestions = emptyList()
                },
                text = {
                    Text(
                        text = suggestion,
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.Start),
                    )
                },
            )
        }
    }
}

private fun isMatch(suggestion: String, prompt: String): Boolean {
    if (suggestion.equals(prompt, ignoreCase = true)) {
        return false
    }
    return suggestion.contains(prompt, ignoreCase = true)
}

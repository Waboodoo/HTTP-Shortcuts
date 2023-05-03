package ch.rmy.android.http_shortcuts.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun HelpText(
    text: String,
) {
    Text(
        text = text,
        fontSize = FontSize.MEDIUM,
    )
}

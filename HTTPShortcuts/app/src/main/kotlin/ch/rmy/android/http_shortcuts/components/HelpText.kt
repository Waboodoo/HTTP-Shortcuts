package ch.rmy.android.http_shortcuts.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun HelpText(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        modifier = modifier,
        text = text,
        fontSize = FontSize.TINY,
        lineHeight = FontSize.SMALL,
    )
}

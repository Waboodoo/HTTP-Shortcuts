package ch.rmy.android.http_shortcuts.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun Label(text: String) {
    Text(
        text,
        fontSize = FontSize.SMALL,
    )
}

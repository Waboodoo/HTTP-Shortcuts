package ch.rmy.android.http_shortcuts.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ScreenInstructionsHeaders(text: String) {
    Box(
        modifier = Modifier.padding(
            horizontal = Spacing.MEDIUM,
            vertical = Spacing.SMALL,
        ),
    ) {
        Text(
            text = text,
            fontSize = FontSize.SMALL,
            lineHeight = FontSize.SMALL,
        )
    }
}

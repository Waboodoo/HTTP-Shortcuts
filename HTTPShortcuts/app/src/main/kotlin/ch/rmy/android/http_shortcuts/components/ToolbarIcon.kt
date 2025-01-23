package ch.rmy.android.http_shortcuts.components

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun ToolbarIcon(
    imageVector: ImageVector,
    contentDescription: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
    ) {
        Icon(imageVector, contentDescription)
    }
}

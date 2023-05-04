package ch.rmy.android.http_shortcuts.components

import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import com.alorma.compose.settings.ui.SettingsMenuLink

@Composable
fun SettingsButton(
    title: String,
    subtitle: String? = null,
    icon: ImageVector,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    SettingsMenuLink(
        icon = { Icon(icon, contentDescription = title) },
        title = { Text(title) },
        subtitle = subtitle?.let { { Text(it) } },
        enabled = enabled,
        onClick = onClick,
    )
}

@Composable
fun SettingsButton(
    title: String,
    subtitle: String? = null,
    icon: Painter? = null,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    SettingsMenuLink(
        icon = icon?.let { { Icon(icon, contentDescription = title) } },
        title = { Text(title) },
        subtitle = subtitle?.let { { Text(it) } },
        enabled = enabled,
        onClick = onClick,
    )
}

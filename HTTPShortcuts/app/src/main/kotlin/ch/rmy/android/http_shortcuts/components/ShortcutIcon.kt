package ch.rmy.android.http_shortcuts.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.extensions.runIf
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun ShortcutIcon(
    shortcutIcon: ShortcutIcon,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    size: Dp = 44.dp,
) {
    val context = LocalContext.current
    val uri = remember(shortcutIcon) {
        shortcutIcon.getIconURI(context).toString()
    }
    val tint = remember(shortcutIcon) {
        (shortcutIcon as? ShortcutIcon.BuiltInIcon)?.tint?.let(::Color)
    }
    val model = remember(uri) {
        ImageRequest.Builder(context)
            .data(uri)
            .fallback(R.drawable.image_placeholder)
            .error(R.drawable.bitsies_cancel)
            .crossfade(true)
            .build()
    }

    AsyncImage(
        model = model,
        contentDescription = contentDescription,
        colorFilter = tint?.let { ColorFilter.tint(tint) },
        modifier = Modifier
            .size(width = size, height = size)
            .then(modifier)
            .runIf(shortcutIcon.isCircular) {
                clip(CircleShape)
            },
    )
}

package ch.rmy.android.http_shortcuts.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ch.rmy.android.framework.extensions.runIfNotNull
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.extensions.runIf
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import coil3.request.fallback
import coil3.request.placeholder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
        shortcutIcon.tint?.let(::Color)
    }
    val model = remember(uri) {
        ImageRequest.Builder(context)
            .data(uri)
            .fallback(R.drawable.image_placeholder)
            .placeholder(R.drawable.image_placeholder)
            .error(R.drawable.bitsies_cancel)
            .crossfade(true)
            .build()
    }

    val isDarkMode = isSystemInDarkTheme()
    var background by remember {
        mutableStateOf<Color?>(null)
    }
    LaunchedEffect(shortcutIcon, isDarkMode) {
        background = withContext(Dispatchers.Default) {
            val iconLuminance = when (shortcutIcon) {
                is ShortcutIcon.CustomIcon -> shortcutIcon.tint ?: shortcutIcon.singleColor
                else -> shortcutIcon.tint
            }
                ?.let(::Color)
                ?.luminance()
                ?: return@withContext null
            when {
                iconLuminance > 0.75f && !isDarkMode -> Color.Black.copy(alpha = 0.7f)
                iconLuminance < 0.07f && isDarkMode -> Color.White.copy(alpha = 0.9f)
                else -> null
            }
        }
    }

    val modifier = Modifier
        .width(size)
        .then(modifier)
        .runIf(shortcutIcon.isCircular) {
            clip(CircleShape)
        }
        .runIfNotNull(background) { background ->
            val offset = size * 0.05f
            background(background, shape = RoundedCornerShape(percent = 30))
                .padding(offset)
        }
        .aspectRatio(1f)

    val inPreview = LocalInspectionMode.current
    if (inPreview) {
        Box(
            modifier = modifier.background(Color.Cyan),
        )
        return
    }

    AsyncImage(
        model = model,
        contentDescription = contentDescription,
        imageLoader = remember(context) { ImageLoader(context) },
        colorFilter = tint?.let { ColorFilter.tint(tint) },
        modifier = modifier,
    )
}

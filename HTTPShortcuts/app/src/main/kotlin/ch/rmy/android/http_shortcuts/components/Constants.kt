package ch.rmy.android.http_shortcuts.components

import androidx.compose.runtime.Stable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Stable
object Spacing {
    val TINY = 4.dp
    val SMALL = 8.dp
    val MEDIUM = 16.dp
    val BIG = 32.dp
    val HUGE = 64.dp
}

@Stable
object FontSize {
    val HUGE = 22.sp
    val BIG = 18.sp
    val MEDIUM = 16.sp
    val SMALL = 14.sp
    val TINY = 12.sp
}

@Stable
val DefaultTextShadow = Shadow(
    offset = Offset(2f, 2f),
    color = Color.Black.copy(alpha = 0.5f),
    blurRadius = 1f,
)

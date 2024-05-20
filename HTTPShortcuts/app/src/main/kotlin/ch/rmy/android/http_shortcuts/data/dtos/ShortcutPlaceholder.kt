package ch.rmy.android.http_shortcuts.data.dtos

import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon

@Stable
data class ShortcutPlaceholder(
    val id: String,
    val name: String,
    val description: String?,
    val icon: ShortcutIcon,
)

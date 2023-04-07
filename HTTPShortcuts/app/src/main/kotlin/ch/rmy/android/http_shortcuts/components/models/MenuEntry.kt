package ch.rmy.android.http_shortcuts.components.models

import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon

@Stable
data class MenuEntry<T : Any>(
    val key: T,
    val name: String,
    val icon: ShortcutIcon? = null,
)

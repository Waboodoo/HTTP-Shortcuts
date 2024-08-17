package ch.rmy.android.http_shortcuts.activities.main.models

import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon

@Stable
data class ShortcutItem(
    val id: ShortcutId,
    val name: String,
    val description: String,
    val icon: ShortcutIcon,
    val isPending: Boolean,
    val isHidden: Boolean,
)

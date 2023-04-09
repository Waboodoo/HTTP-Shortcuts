package ch.rmy.android.http_shortcuts.activities.icons

import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon

@Stable
data class IconPickerListItem(
    val icon: ShortcutIcon.CustomIcon,
    val isUnused: Boolean,
)

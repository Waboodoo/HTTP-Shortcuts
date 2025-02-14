package ch.rmy.android.http_shortcuts.activities.main

import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.activities.main.models.ShortcutListItem
import ch.rmy.android.http_shortcuts.data.enums.CategoryBackgroundType

@Stable
data class ShortcutListViewState(
    val dialogState: ShortcutListDialogState? = null,
    val shortcutListItems: List<ShortcutListItem> = emptyList(),
    val isAppLocked: Boolean = false,
    val background: CategoryBackgroundType = CategoryBackgroundType.Default,
) {
    val isLongClickingEnabled
        get() = !isAppLocked
}

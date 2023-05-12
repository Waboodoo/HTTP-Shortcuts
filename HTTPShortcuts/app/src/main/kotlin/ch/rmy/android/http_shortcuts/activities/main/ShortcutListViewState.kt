package ch.rmy.android.http_shortcuts.activities.main

import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.activities.main.models.ShortcutItem
import ch.rmy.android.http_shortcuts.data.enums.CategoryBackgroundType

@Stable
data class ShortcutListViewState(
    val dialogState: ShortcutListDialogState? = null,
    val shortcuts: List<ShortcutItem> = emptyList(),
    val isAppLocked: Boolean = false,
    val background: CategoryBackgroundType = CategoryBackgroundType.Default,
) {
    val isLongClickingEnabled
        get() = !isAppLocked
}

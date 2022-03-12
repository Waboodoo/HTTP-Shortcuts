package ch.rmy.android.http_shortcuts.activities.main

import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.data.enums.CategoryBackgroundType

data class ShortcutListViewState(
    val dialogState: DialogState? = null,
    val shortcuts: List<ShortcutListItem> = emptyList(),
    val isInMovingMode: Boolean = false,
    val isAppLocked: Boolean = false,
    val background: CategoryBackgroundType = CategoryBackgroundType.WHITE,
) {
    val isDraggingEnabled
        get() = !isAppLocked && isInMovingMode

    val isLongClickingEnabled
        get() = !isAppLocked && !isInMovingMode

    val isEmptyStateVisible
        get() = shortcuts.singleOrNull() is ShortcutListItem.EmptyState
}

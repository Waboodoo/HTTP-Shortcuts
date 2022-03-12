package ch.rmy.android.http_shortcuts.activities.categories

import ch.rmy.android.framework.viewmodel.viewstate.DialogState

data class CategoriesViewState(
    val dialogState: DialogState? = null,
    val categories: List<CategoryListItem>,
) {
    val isDraggingEnabled: Boolean
        get() = categories.size > 1
}

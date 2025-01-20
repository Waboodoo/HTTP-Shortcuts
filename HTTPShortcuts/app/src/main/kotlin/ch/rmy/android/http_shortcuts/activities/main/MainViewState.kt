package ch.rmy.android.http_shortcuts.activities.main

import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.activities.main.models.CategoryItem
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId
import ch.rmy.android.http_shortcuts.data.enums.SelectionMode

@Stable
data class MainViewState(
    val dialogState: MainDialogState? = null,
    val toolbarTitle: String = "",
    val isLocked: Boolean,
    val categoryItems: List<CategoryItem>,
    val hasMultipleCategories: Boolean,
    val selectionMode: SelectionMode,
    val activeCategoryId: CategoryId,
) {
    val isCreateButtonVisible
        get() = !isLocked
}

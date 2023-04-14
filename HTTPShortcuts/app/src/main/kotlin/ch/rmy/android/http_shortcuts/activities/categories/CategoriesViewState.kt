package ch.rmy.android.http_shortcuts.activities.categories

import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.activities.categories.models.CategoryListItem

@Stable
data class CategoriesViewState(
    val dialogState: CategoriesDialogState? = null,
    val categories: List<CategoryListItem>,
)

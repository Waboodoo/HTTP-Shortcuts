package ch.rmy.android.http_shortcuts.activities.categories.sections

import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.activities.categories.sections.models.CategorySectionListItem

@Stable
data class CategorySectionsViewState(
    val dialogState: CategorySectionsDialogState? = null,
    val sectionItems: List<CategorySectionListItem> = emptyList(),
)

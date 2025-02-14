package ch.rmy.android.http_shortcuts.activities.moving.models

import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId
import ch.rmy.android.http_shortcuts.data.domains.sections.SectionId
import ch.rmy.android.http_shortcuts.data.dtos.ShortcutPlaceholder

@Stable
data class CategorySectionItem(
    val id: CategorySectionId,
    val categoryName: String,
    val sectionName: String?,
    val shortcuts: List<ShortcutPlaceholder>,
) {
    data class CategorySectionId(val categoryId: CategoryId, val sectionId: SectionId?)
}

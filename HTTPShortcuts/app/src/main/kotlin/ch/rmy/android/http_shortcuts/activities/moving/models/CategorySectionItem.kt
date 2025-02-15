package ch.rmy.android.http_shortcuts.activities.moving.models

import androidx.compose.runtime.Stable
import ch.rmy.android.framework.extensions.takeUnlessEmpty
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
    data class CategorySectionId(val categoryId: CategoryId, val sectionId: SectionId?) {
        fun serialize() =
            "$PREFIX$categoryId,${sectionId ?: ""}"

        companion object {
            private const val PREFIX = "category_section_"

            fun deserialize(string: String): CategorySectionId? {
                if (!string.startsWith(PREFIX)) {
                    return null
                }
                val (categoryId, sectionId) = string.removePrefix(PREFIX).split(",")
                return CategorySectionId(categoryId, sectionId.takeUnlessEmpty())
            }
        }
    }
}

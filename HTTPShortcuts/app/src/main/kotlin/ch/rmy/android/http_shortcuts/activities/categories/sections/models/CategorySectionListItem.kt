package ch.rmy.android.http_shortcuts.activities.categories.sections.models

import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.data.domains.sections.SectionId

@Stable
data class CategorySectionListItem(
    val id: SectionId,
    val name: String,
)

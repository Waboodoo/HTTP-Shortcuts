package ch.rmy.android.http_shortcuts.activities.moving.models

import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId
import ch.rmy.android.http_shortcuts.data.dtos.ShortcutPlaceholder

@Stable
data class CategoryItem(
    val id: CategoryId,
    val name: String,
    val shortcuts: List<ShortcutPlaceholder>,
)

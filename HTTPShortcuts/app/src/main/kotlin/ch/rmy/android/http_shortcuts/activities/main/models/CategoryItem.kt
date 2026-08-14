package ch.rmy.android.http_shortcuts.activities.main.models

import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId
import ch.rmy.android.http_shortcuts.data.enums.CategoryAlignment
import ch.rmy.android.http_shortcuts.data.enums.CategoryBackgroundType
import ch.rmy.android.http_shortcuts.data.enums.CategoryLayoutType

@Stable
data class CategoryItem(
    val categoryId: CategoryId,
    val name: String,
    val layoutType: CategoryLayoutType,
    val alignment: CategoryAlignment,
    val background: CategoryBackgroundType,
    val scale: Float,
    val hiddenLabels: Boolean,
)

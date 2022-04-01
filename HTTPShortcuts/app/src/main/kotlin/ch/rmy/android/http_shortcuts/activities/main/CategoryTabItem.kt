package ch.rmy.android.http_shortcuts.activities.main

import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId
import ch.rmy.android.http_shortcuts.data.enums.CategoryLayoutType

data class CategoryTabItem(
    val categoryId: CategoryId,
    val name: String,
    val layoutType: CategoryLayoutType,
)

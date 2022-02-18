package ch.rmy.android.http_shortcuts.activities.main

import ch.rmy.android.http_shortcuts.data.enums.CategoryLayoutType

data class CategoryTabItem(
    val categoryId: String,
    val name: String,
    val layoutType: CategoryLayoutType,
)

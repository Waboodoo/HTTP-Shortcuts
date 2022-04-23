package ch.rmy.android.http_shortcuts.activities.categories

import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.http_shortcuts.data.enums.CategoryLayoutType
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon

data class CategoryListItem(
    val id: String,
    val name: Localizable,
    val description: Localizable,
    val icons: List<ShortcutIcon>,
    val layoutType: CategoryLayoutType?,
    val hidden: Boolean,
)

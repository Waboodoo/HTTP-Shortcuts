package ch.rmy.android.http_shortcuts.activities.categories.models

import androidx.compose.runtime.Stable
import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId
import ch.rmy.android.http_shortcuts.data.enums.CategoryLayoutType
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon

@Stable
data class CategoryListItem(
    val id: CategoryId,
    val name: Localizable,
    val description: Localizable,
    val icons: List<ShortcutIcon>,
    val layoutType: CategoryLayoutType?,
    val hidden: Boolean,
)

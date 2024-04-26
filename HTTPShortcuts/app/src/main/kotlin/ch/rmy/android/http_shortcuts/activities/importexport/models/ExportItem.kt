package ch.rmy.android.http_shortcuts.activities.importexport.models

import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon

@Stable
sealed class ExportItem {
    @Stable
    data class Shortcut(
        val shortcutId: ShortcutId,
        val categoryId: CategoryId,
        val name: String,
        val icon: ShortcutIcon? = null,
        val checked: Boolean,
    ) : ExportItem()

    @Stable
    data class Category(
        val categoryId: CategoryId,
        val name: String,
        val checked: Boolean,
    ) : ExportItem()
}

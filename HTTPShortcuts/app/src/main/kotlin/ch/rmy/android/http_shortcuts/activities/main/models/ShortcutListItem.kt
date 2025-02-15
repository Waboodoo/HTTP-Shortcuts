package ch.rmy.android.http_shortcuts.activities.main.models

import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.data.domains.sections.SectionId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon

sealed class ShortcutListItem {
    @Stable
    data class Section(
        val id: SectionId,
        val name: String,
    ) : ShortcutListItem()

    @Stable
    data class EmptyState(
        val id: SectionId,
    ) : ShortcutListItem()

    @Stable
    data class ShortcutItem(
        val id: ShortcutId,
        val name: String,
        val description: String,
        val icon: ShortcutIcon,
        val isPending: Boolean,
        val isHidden: Boolean,
    ) : ShortcutListItem()
}

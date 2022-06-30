package ch.rmy.android.http_shortcuts.activities.editor.shortcuts.models

import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.framework.utils.localization.StringResLocalizable
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon

sealed interface ShortcutListItem {
    data class Shortcut(
        val id: ShortcutListItemId,
        val name: Localizable,
        val icon: ShortcutIcon,
    ) : ShortcutListItem

    object EmptyState : ShortcutListItem {
        val title: Localizable
            get() = StringResLocalizable(R.string.empty_state_trigger_shortcuts)

        val instructions: Localizable
            get() = StringResLocalizable(R.string.empty_state_trigger_shortcuts_instructions)
    }
}

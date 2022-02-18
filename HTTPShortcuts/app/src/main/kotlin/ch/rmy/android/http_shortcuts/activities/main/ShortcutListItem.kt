package ch.rmy.android.http_shortcuts.activities.main

import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.framework.utils.localization.StringResLocalizable
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon

sealed interface ShortcutListItem {
    data class Shortcut(
        val id: String,
        val name: String,
        val description: String,
        val icon: ShortcutIcon,
        val isPending: Boolean,
        val textColor: TextColor,
    ) : ShortcutListItem

    object EmptyState : ShortcutListItem {
        val title: Localizable
            get() = StringResLocalizable(R.string.empty_state_shortcuts)

        val instructions: Localizable
            get() = StringResLocalizable(R.string.empty_state_shortcuts_instructions)
    }

    enum class TextColor {
        BRIGHT,
        DARK,
    }
}

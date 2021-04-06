package ch.rmy.android.http_shortcuts.scripting.shortcuts

import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon

class ShortcutPlaceholder(val id: String, val name: String, val icon: ShortcutIcon) {

    fun isDeleted() = name.isEmpty() && icon == ShortcutIcon.NoIcon

    companion object {

        fun deletedShortcut(id: String) = ShortcutPlaceholder(id, "", ShortcutIcon.NoIcon)

        fun fromShortcut(shortcut: Shortcut) =
            ShortcutPlaceholder(
                id = shortcut.id,
                name = shortcut.name,
                icon = shortcut.icon,
            )

    }

}
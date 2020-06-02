package ch.rmy.android.http_shortcuts.scripting.shortcuts

import ch.rmy.android.http_shortcuts.data.models.Shortcut

class ShortcutPlaceholder(val id: String, val name: String, val iconName: String?) {

    companion object {

        fun fromShortcut(shortcut: Shortcut) =
            ShortcutPlaceholder(
                id = shortcut.id,
                name = shortcut.name,
                iconName = shortcut.iconName
            )

    }

}
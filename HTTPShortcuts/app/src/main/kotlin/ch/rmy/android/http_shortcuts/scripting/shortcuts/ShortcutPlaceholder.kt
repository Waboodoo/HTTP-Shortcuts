package ch.rmy.android.http_shortcuts.scripting.shortcuts

import ch.rmy.android.http_shortcuts.data.models.Shortcut

class ShortcutPlaceholder(val id: String, val name: String, val iconName: String?) {

    fun isDeleted() = name.isEmpty() && iconName == null

    companion object {

        fun deletedShortcut(id: String) = ShortcutPlaceholder(id, "", null)

        fun fromShortcut(shortcut: Shortcut) =
            ShortcutPlaceholder(
                id = shortcut.id,
                name = shortcut.name,
                iconName = shortcut.iconName
            )

    }

}
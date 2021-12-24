package ch.rmy.android.http_shortcuts.scripting.shortcuts

import ch.rmy.android.http_shortcuts.data.models.Shortcut

class ShortcutPlaceholderProvider {

    var placeholders: List<ShortcutPlaceholder> = emptyList()
        private set

    fun applyShortcuts(shortcuts: Collection<Shortcut>) {
        placeholders = shortcuts.map(ShortcutPlaceholder::fromShortcut)
    }

    fun findPlaceholderById(shortcutId: String): ShortcutPlaceholder? =
        placeholders
            .firstOrNull { it.id == shortcutId }
}

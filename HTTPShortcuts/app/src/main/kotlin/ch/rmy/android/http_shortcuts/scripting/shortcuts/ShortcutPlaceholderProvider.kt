package ch.rmy.android.http_shortcuts.scripting.shortcuts

import ch.rmy.android.http_shortcuts.data.models.Shortcut

class ShortcutPlaceholderProvider {

    fun applyShortcuts(shortcuts: Collection<Shortcut>) {
        placeholders = shortcuts.map(ShortcutPlaceholder::fromShortcut)
        cache = placeholders
    }

    var placeholders: List<ShortcutPlaceholder> = cache
        private set

    fun findPlaceholderById(shortcutId: String): ShortcutPlaceholder? =
        placeholders
            .firstOrNull { it.id == shortcutId }

    companion object {
        private var cache: List<ShortcutPlaceholder> = emptyList()
    }
}

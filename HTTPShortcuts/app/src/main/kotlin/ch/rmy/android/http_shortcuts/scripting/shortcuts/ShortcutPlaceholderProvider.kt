package ch.rmy.android.http_shortcuts.scripting.shortcuts

import ch.rmy.android.http_shortcuts.data.models.ShortcutModel

class ShortcutPlaceholderProvider {

    fun applyShortcuts(shortcuts: Collection<ShortcutModel>) {
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

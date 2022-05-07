package ch.rmy.android.http_shortcuts.scripting.shortcuts

import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.models.ShortcutModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShortcutPlaceholderProvider
@Inject
constructor() {

    fun applyShortcuts(shortcuts: Collection<ShortcutModel>) {
        placeholders = shortcuts.map(ShortcutPlaceholder::fromShortcut)
    }

    var placeholders: List<ShortcutPlaceholder> = emptyList()
        private set

    fun findPlaceholderById(shortcutId: ShortcutId): ShortcutPlaceholder? =
        placeholders
            .firstOrNull { it.id == shortcutId }
}

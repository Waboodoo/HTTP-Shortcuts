package ch.rmy.android.http_shortcuts.scripting.shortcuts

import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.dtos.ShortcutPlaceholder
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.extensions.toShortcutPlaceholder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShortcutPlaceholderProvider
@Inject
constructor() {

    fun applyShortcuts(shortcuts: Collection<Shortcut>) {
        placeholders = shortcuts.map(Shortcut::toShortcutPlaceholder)
    }

    var placeholders: List<ShortcutPlaceholder> = emptyList()
        private set

    fun findPlaceholderById(shortcutId: ShortcutId): ShortcutPlaceholder? =
        placeholders
            .firstOrNull { it.id == shortcutId }
}

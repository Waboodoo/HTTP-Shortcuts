package ch.rmy.android.http_shortcuts.scripting.shortcuts

import ch.rmy.android.http_shortcuts.data.livedata.ListLiveData
import ch.rmy.android.http_shortcuts.data.models.Shortcut

class ShortcutPlaceholderProvider(private val shortcuts: ListLiveData<Shortcut>) {

    fun findPlaceholderById(shortcutId: String): ShortcutPlaceholder? =
        shortcuts
            .firstOrNull { it.id == shortcutId }
            ?.let { ShortcutPlaceholder.fromShortcut(it) }

    val placeholders
        get() = shortcuts.map { ShortcutPlaceholder.fromShortcut(it) }
}

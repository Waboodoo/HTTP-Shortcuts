package ch.rmy.android.http_shortcuts.media_controls

import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId

data class MediaShortcutItem(
    val id: ShortcutId,
    val name: String,
    val categoryName: String,
    val description: String,
)

class MediaShortcutQueue {
    private var items = emptyList<MediaShortcutItem>()
    private var selectedIndex = 0

    val selectedItem: MediaShortcutItem?
        get() = items.getOrNull(selectedIndex)

    fun update(newItems: List<MediaShortcutItem>): MediaShortcutItem? {
        val selectedId = selectedItem?.id
        items = newItems
        selectedIndex = selectedId
            ?.let { id -> items.indexOfFirst { it.id == id } }
            ?.takeIf { it >= 0 }
            ?: 0
        return selectedItem
    }

    fun selectNext(): MediaShortcutItem? {
        if (items.isEmpty()) {
            return null
        }
        selectedIndex = (selectedIndex + 1) % items.size
        return selectedItem
    }

    fun selectPrevious(): MediaShortcutItem? {
        if (items.isEmpty()) {
            return null
        }
        selectedIndex = (selectedIndex - 1).mod(items.size)
        return selectedItem
    }
}

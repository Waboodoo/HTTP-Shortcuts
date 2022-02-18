package ch.rmy.android.http_shortcuts.activities.editor.shortcuts

data class TriggerShortcutsViewState(
    val shortcuts: List<ShortcutListItem> = emptyList(),
) {
    val isDraggingEnabled: Boolean
        get() = shortcuts.size > 1
}

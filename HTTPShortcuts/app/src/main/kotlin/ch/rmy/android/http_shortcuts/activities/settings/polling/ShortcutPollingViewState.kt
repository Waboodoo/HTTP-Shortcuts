package ch.rmy.android.http_shortcuts.activities.settings.polling

import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.activities.settings.polling.models.ShortcutListItem

data class ShortcutPollingViewState(
    val dialogState: DialogState? = null,
    val shortcuts: List<ShortcutListItem> = emptyList(),
) {
    val isDraggingEnabled: Boolean
        get() = shortcuts.size > 1
}

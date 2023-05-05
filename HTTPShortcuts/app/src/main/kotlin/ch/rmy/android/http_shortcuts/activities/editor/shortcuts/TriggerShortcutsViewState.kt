package ch.rmy.android.http_shortcuts.activities.editor.shortcuts

import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.activities.editor.shortcuts.models.ShortcutListItem

@Stable
data class TriggerShortcutsViewState(
    val dialogState: TriggerShortcutsDialogState? = null,
    val shortcuts: List<ShortcutListItem> = emptyList(),
)

package ch.rmy.android.http_shortcuts.activities.editor.shortcuts

import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.activities.editor.shortcuts.models.ShortcutListItemId
import ch.rmy.android.http_shortcuts.data.dtos.ShortcutPlaceholder

@Stable
sealed class TriggerShortcutsDialogState {
    @Stable
    data class AddShortcuts(
        val shortcuts: List<ShortcutPlaceholder>,
    ) : TriggerShortcutsDialogState()

    @Stable
    data class DeleteShortcut(
        val id: ShortcutListItemId,
        val name: String?,
    ) : TriggerShortcutsDialogState()
}

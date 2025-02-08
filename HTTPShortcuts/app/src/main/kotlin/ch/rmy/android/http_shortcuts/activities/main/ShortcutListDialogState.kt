package ch.rmy.android.http_shortcuts.activities.main

import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId

@Stable
sealed class ShortcutListDialogState {
    @Stable
    data class CurlExport(
        val shortcutName: String,
        val command: String,
    ) : ShortcutListDialogState()

    @Stable
    data class ShortcutInfo(
        val shortcutId: ShortcutId,
        val shortcutName: String,
    ) : ShortcutListDialogState()

    @Stable
    data class Deletion(
        val shortcutName: String,
    ) : ShortcutListDialogState()

    @Stable
    data object ExportOptions : ShortcutListDialogState()

    @Stable
    data object ExportDestinationOptions : ShortcutListDialogState()

    @Stable
    data class ContextMenu(
        val shortcutName: String,
        val isPending: Boolean,
        val isHidden: Boolean,
    ) : ShortcutListDialogState()

    @Stable
    data class ExportError(
        val message: String,
    ) : ShortcutListDialogState()

    @Stable
    data object ExportProgress : ShortcutListDialogState()

    @Stable
    data object ShortcutUnhideInstructions : ShortcutListDialogState()
}

package ch.rmy.android.http_shortcuts.activities.editor

import androidx.compose.runtime.Stable

@Stable
sealed class ShortcutEditorDialogState {
    @Stable
    object DiscardWarning : ShortcutEditorDialogState()

    @Stable
    data class PickIcon(
        val includeFaviconOption: Boolean,
    ) : ShortcutEditorDialogState()
}

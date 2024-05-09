package ch.rmy.android.http_shortcuts.activities.editor

import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon

@Stable
sealed class ShortcutEditorDialogState {
    @Stable
    data object DiscardWarning : ShortcutEditorDialogState()

    @Stable
    data class PickIcon(
        val currentIcon: ShortcutIcon.BuiltInIcon?,
        val suggestionBase: String?,
        val includeFaviconOption: Boolean,
    ) : ShortcutEditorDialogState()
}

package ch.rmy.android.http_shortcuts.activities.icons

import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon

@Stable
sealed class IconPickerDialogState {
    data object SelectShape : IconPickerDialogState()

    data class DeleteIcon(
        val icon: ShortcutIcon.CustomIcon,
        val stillInUseWarning: Boolean,
    ) : IconPickerDialogState()

    data object BulkDelete : IconPickerDialogState()
}

package ch.rmy.android.http_shortcuts.activities.icons

import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon

@Stable
sealed class IconPickerDialogState {
    @Stable
    data object SelectShape : IconPickerDialogState()

    @Stable
    data object MaterialIconsInfo : IconPickerDialogState()

    @Stable
    data object SelectMaterialIcon : IconPickerDialogState()

    @Stable
    data class CustomIconColorPicker(val selectedIcon: ShortcutIcon.CustomIcon) : IconPickerDialogState()

    @Stable
    data class DeleteIcon(
        val icon: ShortcutIcon.CustomIcon,
        val stillInUseWarning: Boolean,
    ) : IconPickerDialogState()

    @Stable
    data object BulkDelete : IconPickerDialogState()

    @Stable
    data object Processing : IconPickerDialogState()
}

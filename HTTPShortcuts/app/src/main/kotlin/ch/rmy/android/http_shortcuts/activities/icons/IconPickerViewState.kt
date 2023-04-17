package ch.rmy.android.http_shortcuts.activities.icons

import androidx.compose.runtime.Stable

@Stable
data class IconPickerViewState(
    val dialogState: IconPickerDialogState? = null,
    val icons: List<IconPickerListItem>,
) {
    val isDeleteButtonEnabled: Boolean
        get() = icons.any { it.isUnused }
}

package ch.rmy.android.http_shortcuts.activities.icons

import ch.rmy.android.framework.viewmodel.viewstate.DialogState

data class IconPickerViewState(
    val dialogState: DialogState? = null,
    val icons: List<IconPickerListItem>,
) {
    val isEmptyStateVisible: Boolean
        get() = icons.isEmpty()

    val isDeleteButtonVisible: Boolean
        get() = icons.any { it.isUnused }
}

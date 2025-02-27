package ch.rmy.android.http_shortcuts.activities.categories

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.ConfirmDialog
import ch.rmy.android.http_shortcuts.components.IconPickerDialog
import ch.rmy.android.http_shortcuts.components.SelectDialog
import ch.rmy.android.http_shortcuts.components.SelectDialogEntry
import ch.rmy.android.http_shortcuts.extensions.localize
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon

@Composable
fun CategoriesDialogs(
    dialogState: CategoriesDialogState?,
    onEditClicked: () -> Unit,
    onManageSectionsClicked: () -> Unit,
    onVisibilityChangeRequested: (Boolean) -> Unit,
    onPlaceOnHomeScreenClicked: () -> Unit,
    onDeleteClicked: () -> Unit,
    onDeletionConfirmed: () -> Unit,
    onIconSelected: (ShortcutIcon) -> Unit,
    onCustomIconOptionSelected: () -> Unit,
    onDismissRequested: () -> Unit,
) {
    when (dialogState) {
        is CategoriesDialogState.ContextMenu -> {
            ContextMenuDialog(
                dialogState,
                onEditClicked,
                onManageSectionsClicked,
                onVisibilityChangeRequested,
                onPlaceOnHomeScreenClicked,
                onDeleteClicked,
                onDismissRequested,
            )
        }
        is CategoriesDialogState.Deletion -> {
            DeletionConfirmDialog(
                title = dialogState.title,
                onConfirmed = onDeletionConfirmed,
                onDismissRequested = onDismissRequested,
            )
        }
        is CategoriesDialogState.IconPicker -> {
            IconPickerDialog(
                currentIcon = dialogState.currentIcon,
                suggestionBase = dialogState.suggestionBase,
                title = stringResource(R.string.title_category_select_icon),
                onCustomIconOptionSelected = onCustomIconOptionSelected,
                onIconSelected = onIconSelected,
                onDismissRequested = onDismissRequested,
            )
        }
        null -> Unit
    }
}

@Composable
private fun ContextMenuDialog(
    contextMenuState: CategoriesDialogState.ContextMenu,
    onEditClicked: () -> Unit,
    onManageSectionsClicked: () -> Unit,
    onVisibilityChangeRequested: (Boolean) -> Unit,
    onPlaceOnHomeScreenClicked: () -> Unit,
    onDeleteClicked: () -> Unit,
    onDismissRequested: () -> Unit,
) {
    SelectDialog(
        title = contextMenuState.title.localize(),
        onDismissRequest = onDismissRequested,
    ) {
        SelectDialogEntry(
            label = stringResource(R.string.action_edit),
            icon = Icons.Filled.Edit,
            onClick = onEditClicked,
        )
        SelectDialogEntry(
            label = stringResource(R.string.action_manage_sections),
            icon = Icons.Filled.Menu,
            onClick = onManageSectionsClicked,
        )
        if (contextMenuState.placeOnHomeScreenOptionVisible) {
            SelectDialogEntry(
                label = stringResource(R.string.action_place_category),
                icon = Icons.Filled.Home,
                onClick = onPlaceOnHomeScreenClicked,
            )
        }
        if (contextMenuState.showOptionVisible) {
            SelectDialogEntry(
                label = stringResource(R.string.action_show_category),
                icon = Icons.Filled.Visibility,
                onClick = {
                    onVisibilityChangeRequested(true)
                },
            )
        }
        if (contextMenuState.hideOptionVisible) {
            SelectDialogEntry(
                label = stringResource(R.string.action_hide_category),
                icon = Icons.Filled.VisibilityOff,
                enabled = contextMenuState.hideOptionEnabled,
                onClick = {
                    onVisibilityChangeRequested(false)
                },
            )
        }
        SelectDialogEntry(
            label = stringResource(R.string.action_delete),
            icon = Icons.Filled.Delete,
            onClick = onDeleteClicked,
            enabled = contextMenuState.deleteOptionEnabled,
        )
    }
}

@Composable
private fun DeletionConfirmDialog(
    title: String,
    onConfirmed: () -> Unit,
    onDismissRequested: () -> Unit,
) {
    ConfirmDialog(
        title = title,
        message = stringResource(R.string.confirm_delete_category_message),
        confirmButton = stringResource(R.string.dialog_delete),
        onConfirmRequest = onConfirmed,
        onDismissRequest = onDismissRequested,
    )
}

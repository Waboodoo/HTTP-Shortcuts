package ch.rmy.android.http_shortcuts.activities.categories

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
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
    onMaterialDesignIconOptionSelected: () -> Unit,
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
                onMaterialDesignIconOptionSelected = onMaterialDesignIconOptionSelected,
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
    ) { horizontalPadding ->
        SelectDialogEntry(
            horizontalPadding = horizontalPadding,
            label = stringResource(R.string.action_edit),
            icon = painterResource(R.drawable.outline_edit_24),
            onClick = onEditClicked,
        )
        SelectDialogEntry(
            horizontalPadding = horizontalPadding,
            label = stringResource(R.string.action_manage_sections),
            icon = painterResource(R.drawable.outline_menu_24),
            onClick = onManageSectionsClicked,
        )
        if (contextMenuState.placeOnHomeScreenOptionVisible) {
            SelectDialogEntry(
                horizontalPadding = horizontalPadding,
                label = stringResource(R.string.action_place_category),
                icon = painterResource(R.drawable.outline_home_24),
                onClick = onPlaceOnHomeScreenClicked,
            )
        }
        if (contextMenuState.showOptionVisible) {
            SelectDialogEntry(
                horizontalPadding = horizontalPadding,
                label = stringResource(R.string.action_show_category),
                icon = painterResource(R.drawable.outline_visibility_24),
                onClick = {
                    onVisibilityChangeRequested(true)
                },
            )
        }
        if (contextMenuState.hideOptionVisible) {
            SelectDialogEntry(
                horizontalPadding = horizontalPadding,
                label = stringResource(R.string.action_hide_category),
                icon = painterResource(R.drawable.outline_visibility_off_24),
                enabled = contextMenuState.hideOptionEnabled,
                onClick = {
                    onVisibilityChangeRequested(false)
                },
            )
        }
        SelectDialogEntry(
            horizontalPadding = horizontalPadding,
            label = stringResource(R.string.action_delete),
            icon = painterResource(R.drawable.outline_delete_24),
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

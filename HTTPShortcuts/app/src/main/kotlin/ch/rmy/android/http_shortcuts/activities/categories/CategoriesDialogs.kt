package ch.rmy.android.http_shortcuts.activities.categories

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ch.rmy.android.framework.extensions.launch
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.icons.IconPickerActivity
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
    onVisibilityChangeRequested: (Boolean) -> Unit,
    onPlaceOnHomeScreenClicked: () -> Unit,
    onDeleteClicked: () -> Unit,
    onDeletionConfirmed: () -> Unit,
    onIconSelected: (ShortcutIcon) -> Unit,
    onDismissRequested: () -> Unit,
) {
    val pickCustomIcon = rememberLauncherForActivityResult(IconPickerActivity.PickIcon) { icon ->
        if (icon != null) {
            onIconSelected(icon)
        } else {
            onDismissRequested()
        }
    }

    when (dialogState) {
        is CategoriesDialogState.ContextMenu -> {
            ContextMenuDialog(
                dialogState,
                onEditClicked,
                onVisibilityChangeRequested,
                onPlaceOnHomeScreenClicked,
                onDeleteClicked,
                onDismissRequested,
            )
        }
        is CategoriesDialogState.Deletion -> {
            DeletionConfirmDialog(
                onDeletionConfirmed,
                onDismissRequested,
            )
        }
        is CategoriesDialogState.IconPicker -> {
            IconPickerDialog(
                title = stringResource(R.string.title_category_select_icon),
                onCustomIconOptionSelected = {
                    pickCustomIcon.launch()
                },
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
            onClick = onEditClicked,
        )
        if (contextMenuState.showOptionVisible) {
            SelectDialogEntry(
                label = stringResource(R.string.action_show_category),
                onClick = {
                    onVisibilityChangeRequested(true)
                },
            )
        }
        if (contextMenuState.hideOptionVisible) {
            SelectDialogEntry(
                label = stringResource(R.string.action_hide_category),
                onClick = {
                    onVisibilityChangeRequested(false)
                },
            )
        }
        if (contextMenuState.placeOnHomeScreenOptionVisible) {
            SelectDialogEntry(
                label = stringResource(R.string.action_place_category),
                onClick = onPlaceOnHomeScreenClicked,
            )
        }
        SelectDialogEntry(
            label = stringResource(R.string.action_delete),
            onClick = onDeleteClicked,
            enabled = contextMenuState.deleteOptionEnabled,
        )
    }
}

@Composable
private fun DeletionConfirmDialog(
    onConfirmed: () -> Unit,
    onDismissRequested: () -> Unit,
) {
    ConfirmDialog(
        message = stringResource(R.string.confirm_delete_category_message),
        confirmButton = stringResource(R.string.dialog_delete),
        onConfirmRequest = onConfirmed,
        onDismissRequest = onDismissRequested,
    )
}

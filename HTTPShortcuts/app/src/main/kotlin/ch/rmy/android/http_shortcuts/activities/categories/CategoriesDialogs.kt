package ch.rmy.android.http_shortcuts.activities.categories

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.ConfirmDialog
import ch.rmy.android.http_shortcuts.components.IconPickerDialog
import ch.rmy.android.http_shortcuts.components.MenuDialogEntry
import ch.rmy.android.http_shortcuts.components.SelectDialog
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
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            MenuDialogEntry(
                label = stringResource(R.string.action_edit),
                onClick = onEditClicked,
            )
            if (contextMenuState.showOptionVisible) {
                Divider()
                MenuDialogEntry(
                    label = stringResource(R.string.action_show_category),
                    onClick = {
                        onVisibilityChangeRequested(true)
                    },
                )
            }
            if (contextMenuState.hideOptionVisible) {
                Divider()
                MenuDialogEntry(
                    label = stringResource(R.string.action_hide_category),
                    onClick = {
                        onVisibilityChangeRequested(false)
                    },
                )
            }
            if (contextMenuState.placeOnHomeScreenOptionVisible) {
                Divider()
                MenuDialogEntry(
                    label = stringResource(R.string.action_place_category),
                    onClick = onPlaceOnHomeScreenClicked,
                )
            }
            if (contextMenuState.deleteOptionVisible) {
                Divider()
                MenuDialogEntry(
                    label = stringResource(R.string.action_delete),
                    onClick = onDeleteClicked,
                )
            }
        }
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

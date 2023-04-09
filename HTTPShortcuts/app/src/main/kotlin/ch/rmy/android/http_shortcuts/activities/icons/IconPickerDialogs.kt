package ch.rmy.android.http_shortcuts.activities.icons

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.ConfirmDialog

@Composable
fun IconPickerDialogs(
    dialogState: IconPickerDialogState?,
    onDeleteConfirmed: () -> Unit,
    onDialogDismissRequested: () -> Unit,
) {
    when (dialogState) {
        is IconPickerDialogState.DeleteIcon -> {
            DeleteIconDialog(
                dialogState.stillInUseWarning,
                onDeleteConfirmed,
                onDialogDismissRequested,
            )
        }
        is IconPickerDialogState.BulkDelete -> {
            BulkDeleteDialog(
                onDeleteConfirmed,
                onDialogDismissRequested,
            )
        }
        null -> Unit
    }
}

@Composable
private fun DeleteIconDialog(
    stillInUseWarning: Boolean,
    onConfirm: () -> Unit,
    onDismissRequested: () -> Unit,
) {
    ConfirmDialog(
        stringResource(
            if (stillInUseWarning) {
                R.string.confirm_delete_custom_icon_still_in_use_message
            } else {
                R.string.confirm_delete_custom_icon_message
            }
        ),
        stringResource(R.string.dialog_delete),
        onConfirm,
        onDismissRequested,
    )
}

@Composable
private fun BulkDeleteDialog(
    onConfirm: () -> Unit,
    onDismissRequested: () -> Unit,
) {
    ConfirmDialog(
        stringResource(R.string.confirm_delete_all_unused_custom_icons_message),
        stringResource(R.string.dialog_delete),
        onConfirm,
        onDismissRequested,
    )
}

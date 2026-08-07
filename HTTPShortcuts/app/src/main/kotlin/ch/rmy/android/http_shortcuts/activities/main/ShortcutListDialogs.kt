package ch.rmy.android.http_shortcuts.activities.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.ColorPickerDialog
import ch.rmy.android.http_shortcuts.components.ConfirmDialog
import ch.rmy.android.http_shortcuts.components.FontSize
import ch.rmy.android.http_shortcuts.components.HelpText
import ch.rmy.android.http_shortcuts.components.MessageDialog
import ch.rmy.android.http_shortcuts.components.ProgressDialog
import ch.rmy.android.http_shortcuts.components.SelectDialog
import ch.rmy.android.http_shortcuts.components.SelectDialogEntry
import ch.rmy.android.http_shortcuts.components.ShortcutIcon
import ch.rmy.android.http_shortcuts.components.Spacing
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon

@Composable
fun ShortcutListDialogs(
    dialogState: ShortcutListDialogState?,
    isInSyncReplaceMode: Boolean,
    onPlaceOnHomeScreenOptionSelected: () -> Unit,
    onExecuteOptionSelected: () -> Unit,
    onCancelPendingExecutionOptionSelected: () -> Unit,
    onEditOptionSelected: () -> Unit,
    onMoveOptionSelected: () -> Unit,
    onDuplicateOptionSelected: () -> Unit,
    onShowSelected: () -> Unit,
    onHideSelected: () -> Unit,
    onDeleteOptionSelected: () -> Unit,
    onShowInfoOptionSelected: () -> Unit,
    onExportOptionSelected: () -> Unit,
    onExportToFileOptionSelected: () -> Unit,
    onExportViaSharingOptionSelected: () -> Unit,
    onExportAsCurlOptionSelected: () -> Unit,
    onExportAsFileOptionSelected: () -> Unit,
    onDeletionConfirmed: () -> Unit,
    onCurlExportCopyButtonClicked: () -> Unit,
    onCurlExportShareButtonClicked: () -> Unit,
    onBackgroundColorSelected: (Int) -> Unit,
    onDismissed: () -> Unit,
) {
    when (dialogState) {
        is ShortcutListDialogState.CurlExport -> {
            CurlExportDialog(
                shortcutName = dialogState.shortcutName,
                command = dialogState.command,
                onCopyButtonClicked = onCurlExportCopyButtonClicked,
                onShareButtonClicked = onCurlExportShareButtonClicked,
                onDismissed = onDismissed,
            )
        }
        is ShortcutListDialogState.ShortcutInfo -> {
            ShortcutInfoDialog(
                shortcutId = dialogState.shortcutId,
                shortcutName = dialogState.shortcutName,
                onDismissed = onDismissed,
            )
        }
        is ShortcutListDialogState.Deletion -> {
            DeletionDialog(
                shortcutName = dialogState.shortcutName,
                onConfirmed = onDeletionConfirmed,
                onDismissed = onDismissed,
            )
        }
        is ShortcutListDialogState.ExportOptions -> {
            ExportOptionsDialog(
                onExportAsCurlOptionSelected = onExportAsCurlOptionSelected,
                onExportAsFileOptionSelected = onExportAsFileOptionSelected,
                onDismissed = onDismissed,
            )
        }
        is ShortcutListDialogState.ExportDestinationOptions -> {
            ExportDestinationOptionsDialog(
                onExportToFileOptionSelected = onExportToFileOptionSelected,
                onExportViaSharingOptionSelected = onExportViaSharingOptionSelected,
                onDismissed = onDismissed,
            )
        }
        is ShortcutListDialogState.ContextMenu -> {
            ContextMenuDialog(
                shortcutName = dialogState.shortcutName,
                isInSyncReplaceMode = isInSyncReplaceMode,
                isPending = dialogState.isPending,
                isHidden = dialogState.isHidden,
                onPlaceOnHomeScreenOptionSelected = onPlaceOnHomeScreenOptionSelected,
                onExecuteOptionSelected = onExecuteOptionSelected,
                onCancelPendingExecutionOptionSelected = onCancelPendingExecutionOptionSelected,
                onEditOptionSelected = onEditOptionSelected,
                onMoveOptionSelected = onMoveOptionSelected,
                onDuplicateOptionSelected = onDuplicateOptionSelected,
                onShowSelected = onShowSelected,
                onHideSelected = onHideSelected,
                onDeleteOptionSelected = onDeleteOptionSelected,
                onShowInfoOptionSelected = onShowInfoOptionSelected,
                onExportOptionSelected = onExportOptionSelected,
                onDismissed = onDismissed,
            )
        }
        is ShortcutListDialogState.SelectBackgroundColor -> {
            PlaceOnHomeScreenBackgroundColorPickerDialog(
                shortcutIcon = dialogState.icon,
                initialColor = dialogState.previousColor,
                onBackgroundColorSelected = onBackgroundColorSelected,
                onDismissed = onDismissed,
            )
        }
        is ShortcutListDialogState.ExportError -> {
            MessageDialog(
                message = stringResource(R.string.export_failed_with_reason, dialogState.message),
                onDismissRequest = onDismissed,
            )
        }
        is ShortcutListDialogState.ExportProgress -> {
            ProgressDialog(
                text = stringResource(R.string.export_in_progress),
                onDismissRequest = onDismissed,
            )
        }
        is ShortcutListDialogState.ShortcutUnhideInstructions -> {
            MessageDialog(
                message = stringResource(R.string.instructions_shortcut_unhiding, stringResource(R.string.settings_title_show_hidden_shortcuts)),
                onDismissRequest = onDismissed,
            )
        }
        null -> Unit
    }
}

@Composable
private fun CurlExportDialog(
    shortcutName: String,
    command: String,
    onCopyButtonClicked: () -> Unit,
    onShareButtonClicked: () -> Unit,
    onDismissed: () -> Unit,
) {
    AlertDialog(
        title = {
            Text(shortcutName)
        },
        text = {
            TextField(
                modifier = Modifier
                    .fillMaxWidth(),
                value = command,
                onValueChange = {},
                textStyle = TextStyle(
                    fontSize = FontSize.SMALL,
                    fontFamily = FontFamily.Monospace,
                ),
                readOnly = true,
            )
        },
        confirmButton = {
            TextButton(onClick = onCopyButtonClicked) {
                Text(stringResource(R.string.button_copy_curl_export))
            }
        },
        dismissButton = {
            TextButton(onClick = onShareButtonClicked) {
                Text(stringResource(R.string.share_button))
            }
        },
        onDismissRequest = onDismissed,
    )
}

@Composable
private fun ShortcutInfoDialog(
    shortcutId: ShortcutId,
    shortcutName: String,
    onDismissed: () -> Unit,
) {
    AlertDialog(
        title = {
            Text(shortcutName)
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(Spacing.SMALL),
            ) {
                TextField(
                    modifier = Modifier
                        .fillMaxWidth(),
                    label = {
                        Text(stringResource(R.string.label_shortcut_id))
                    },
                    value = shortcutId,
                    onValueChange = {},
                    textStyle = TextStyle(
                        fontSize = FontSize.SMALL,
                        fontFamily = FontFamily.Monospace,
                    ),
                    readOnly = true,
                )

                TextField(
                    modifier = Modifier
                        .fillMaxWidth(),
                    label = {
                        Text(stringResource(R.string.label_deep_link_url))
                    },
                    value = "http-shortcuts://$shortcutId",
                    onValueChange = {},
                    textStyle = TextStyle(
                        fontSize = FontSize.SMALL,
                        fontFamily = FontFamily.Monospace,
                    ),
                    readOnly = true,
                )

                HelpText(stringResource(R.string.message_deep_link_instructions))
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissed) {
                Text(stringResource(R.string.dialog_ok))
            }
        },
        onDismissRequest = onDismissed,
    )
}

@Composable
private fun DeletionDialog(
    shortcutName: String,
    onConfirmed: () -> Unit,
    onDismissed: () -> Unit,
) {
    ConfirmDialog(
        title = shortcutName,
        message = stringResource(R.string.confirm_delete_shortcut_message),
        confirmButton = stringResource(R.string.dialog_delete),
        onConfirmRequest = onConfirmed,
        onDismissRequest = onDismissed,
    )
}

@Composable
private fun ExportOptionsDialog(
    onExportAsCurlOptionSelected: () -> Unit,
    onExportAsFileOptionSelected: () -> Unit,
    onDismissed: () -> Unit,
) {
    SelectDialog(
        title = stringResource(R.string.title_export_shortcut_as),
        onDismissRequest = onDismissed,
    ) { horizontalPadding ->
        SelectDialogEntry(
            horizontalPadding = horizontalPadding,
            label = stringResource(R.string.action_export_as_curl),
            onClick = onExportAsCurlOptionSelected,
        )
        SelectDialogEntry(
            horizontalPadding = horizontalPadding,
            label = stringResource(R.string.action_export_as_file),
            onClick = onExportAsFileOptionSelected,
        )
    }
}

@Composable
private fun ExportDestinationOptionsDialog(
    onExportToFileOptionSelected: () -> Unit,
    onExportViaSharingOptionSelected: () -> Unit,
    onDismissed: () -> Unit,
) {
    SelectDialog(
        title = stringResource(R.string.title_export),
        onDismissRequest = onDismissed,
    ) { horizontalPadding ->
        SelectDialogEntry(
            horizontalPadding = horizontalPadding,
            label = stringResource(R.string.button_export_to_general),
            onClick = onExportToFileOptionSelected,
        )
        SelectDialogEntry(
            horizontalPadding = horizontalPadding,
            label = stringResource(R.string.button_export_send_to),
            onClick = onExportViaSharingOptionSelected,
        )
    }
}

@Composable
private fun ContextMenuDialog(
    shortcutName: String,
    isInSyncReplaceMode: Boolean,
    isPending: Boolean,
    isHidden: Boolean,
    onPlaceOnHomeScreenOptionSelected: () -> Unit,
    onExecuteOptionSelected: () -> Unit,
    onCancelPendingExecutionOptionSelected: () -> Unit,
    onEditOptionSelected: () -> Unit,
    onMoveOptionSelected: () -> Unit,
    onDuplicateOptionSelected: () -> Unit,
    onShowSelected: () -> Unit,
    onHideSelected: () -> Unit,
    onDeleteOptionSelected: () -> Unit,
    onShowInfoOptionSelected: () -> Unit,
    onExportOptionSelected: () -> Unit,
    onDismissed: () -> Unit,
) {
    SelectDialog(
        title = shortcutName,
        onDismissRequest = onDismissed,
    ) { horizontalPadding ->
        SelectDialogEntry(
            horizontalPadding = horizontalPadding,
            label = stringResource(R.string.action_place),
            icon = painterResource(R.drawable.outline_add_to_home_screen),
            onClick = onPlaceOnHomeScreenOptionSelected,
        )
        SelectDialogEntry(
            horizontalPadding = horizontalPadding,
            label = stringResource(R.string.action_run),
            icon = painterResource(R.drawable.outline_play_arrow_24),
            onClick = onExecuteOptionSelected,
        )
        if (isPending) {
            SelectDialogEntry(
                horizontalPadding = horizontalPadding,
                label = stringResource(R.string.action_cancel_pending),
                icon = painterResource(R.drawable.outline_cancel_24),
                onClick = onCancelPendingExecutionOptionSelected,
            )
        }
        HorizontalDivider(
            modifier = Modifier
                .padding(horizontal = horizontalPadding)
                .padding(vertical = Spacing.MEDIUM),
        )
        SelectDialogEntry(
            horizontalPadding = horizontalPadding,
            enabled = !isInSyncReplaceMode,
            label = stringResource(R.string.action_edit),
            icon = painterResource(R.drawable.outline_edit_24),
            onClick = onEditOptionSelected,
        )
        SelectDialogEntry(
            horizontalPadding = horizontalPadding,
            enabled = !isInSyncReplaceMode,
            label = stringResource(R.string.action_move),
            icon = painterResource(R.drawable.outline_move_down_24),
            onClick = onMoveOptionSelected,
        )
        SelectDialogEntry(
            horizontalPadding = horizontalPadding,
            enabled = !isInSyncReplaceMode,
            label = stringResource(R.string.action_duplicate),
            icon = painterResource(R.drawable.outline_file_copy_24),
            onClick = onDuplicateOptionSelected,
        )
        if (isHidden) {
            SelectDialogEntry(
                horizontalPadding = horizontalPadding,
                enabled = !isInSyncReplaceMode,
                label = stringResource(R.string.action_show_shortcut),
                icon = painterResource(R.drawable.outline_visibility_24),
                onClick = onShowSelected,
            )
        } else {
            SelectDialogEntry(
                horizontalPadding = horizontalPadding,
                enabled = !isInSyncReplaceMode,
                label = stringResource(R.string.action_hide_shortcut),
                icon = painterResource(R.drawable.outline_visibility_off_24),
                onClick = onHideSelected,
            )
        }
        SelectDialogEntry(
            horizontalPadding = horizontalPadding,
            enabled = !isInSyncReplaceMode,
            label = stringResource(R.string.action_delete),
            icon = painterResource(R.drawable.outline_delete_24),
            onClick = onDeleteOptionSelected,
        )
        HorizontalDivider(
            modifier = Modifier
                .padding(horizontal = horizontalPadding)
                .padding(vertical = Spacing.MEDIUM),
        )
        SelectDialogEntry(
            horizontalPadding = horizontalPadding,
            label = stringResource(R.string.action_shortcut_information),
            icon = painterResource(R.drawable.outline_info_24),
            onClick = onShowInfoOptionSelected,
        )
        SelectDialogEntry(
            horizontalPadding = horizontalPadding,
            label = stringResource(R.string.action_export),
            icon = painterResource(R.drawable.outline_output_24),
            onClick = onExportOptionSelected,
        )
    }
}

@Composable
private fun PlaceOnHomeScreenBackgroundColorPickerDialog(
    shortcutIcon: ShortcutIcon.CustomIcon,
    initialColor: Int,
    onBackgroundColorSelected: (Int) -> Unit,
    onDismissed: () -> Unit,
) {
    ColorPickerDialog(
        title = stringResource(R.string.dialog_title_select_icon_background_color),
        initialColor = remember(initialColor) { initialColor },
        extraContent = { color ->
            Box(
                modifier = Modifier
                    .background(
                        color = Color(color),
                        shape = RoundedCornerShape(percent = 33),
                    )
                    .padding(6.dp),
                contentAlignment = Alignment.Center,
            ) {
                ShortcutIcon(shortcutIcon)
            }
        },
        onColorSelected = { color ->
            onBackgroundColorSelected(color)
        },
        onDismissRequested = onDismissed,
    )
}

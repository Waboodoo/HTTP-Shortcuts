package ch.rmy.android.http_shortcuts.activities.variables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.variables.VariableTypeMappings.getTypeDescription
import ch.rmy.android.http_shortcuts.activities.variables.VariableTypeMappings.getTypeName
import ch.rmy.android.http_shortcuts.components.ConfirmDialog
import ch.rmy.android.http_shortcuts.components.SelectDialog
import ch.rmy.android.http_shortcuts.components.SelectDialogEntry
import ch.rmy.android.http_shortcuts.data.enums.VariableType

@Composable
fun VariablesDialogs(
    dialogState: VariablesDialogState?,
    onVariableTypeSelected: (VariableType) -> Unit,
    onEditClicked: () -> Unit,
    onDuplicateClicked: () -> Unit,
    onDeleteClicked: () -> Unit,
    onDeleteConfirmed: () -> Unit,
    onDismissed: () -> Unit,
) {
    when (dialogState) {
        is VariablesDialogState.ContextMenu -> {
            ContextMenuDialog(
                title = dialogState.variableKey,
                onEditClicked = onEditClicked,
                onDuplicateClicked = onDuplicateClicked,
                onDeleteClicked = onDeleteClicked,
                onDismissed = onDismissed,
            )
        }
        is VariablesDialogState.Creation -> {
            CreationDialog(
                onVariableTypeSelected = onVariableTypeSelected,
                onDismissed = onDismissed,
            )
        }
        is VariablesDialogState.Delete -> {
            DeletionDialog(
                shortcutNames = dialogState.shortcutNames,
                onConfirmed = onDeleteConfirmed,
                onDismissed = onDismissed,
            )
        }
        null -> Unit
    }
}

@Composable
private fun ContextMenuDialog(
    title: String,
    onEditClicked: () -> Unit,
    onDuplicateClicked: () -> Unit,
    onDeleteClicked: () -> Unit,
    onDismissed: () -> Unit,
) {
    SelectDialog(
        title = title,
        onDismissRequest = onDismissed,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            SelectDialogEntry(
                label = stringResource(R.string.action_edit),
                onClick = onEditClicked,
            )
            SelectDialogEntry(
                label = stringResource(R.string.action_duplicate),
                onClick = onDuplicateClicked,
            )
            SelectDialogEntry(
                label = stringResource(R.string.action_delete),
                onClick = onDeleteClicked,
            )
        }
    }
}

@Composable
private fun CreationDialog(
    onVariableTypeSelected: (VariableType) -> Unit,
    onDismissed: () -> Unit,
) {
    SelectDialog(
        title = stringResource(R.string.title_select_variable_type),
        onDismissRequest = onDismissed,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
        ) {
            SelectDialogEntry(
                label = stringResource(VariableType.CONSTANT.getTypeName()),
                description = stringResource(VariableType.CONSTANT.getTypeDescription()),
                onClick = {
                    onVariableTypeSelected(VariableType.CONSTANT)
                },
            )
            Divider()
            SelectDialogEntry(
                label = stringResource(VariableType.SELECT.getTypeName()),
                description = stringResource(VariableType.SELECT.getTypeDescription()),
                onClick = {
                    onVariableTypeSelected(VariableType.SELECT)
                },
            )
            SelectDialogEntry(
                label = stringResource(VariableType.TEXT.getTypeName()),
                description = stringResource(VariableType.TEXT.getTypeDescription()),
                onClick = {
                    onVariableTypeSelected(VariableType.TEXT)
                },
            )
            SelectDialogEntry(
                label = stringResource(VariableType.NUMBER.getTypeName()),
                description = stringResource(VariableType.NUMBER.getTypeDescription()),
                onClick = {
                    onVariableTypeSelected(VariableType.NUMBER)
                },
            )
            SelectDialogEntry(
                label = stringResource(VariableType.SLIDER.getTypeName()),
                description = stringResource(VariableType.SLIDER.getTypeDescription()),
                onClick = {
                    onVariableTypeSelected(VariableType.SLIDER)
                },
            )
            SelectDialogEntry(
                label = stringResource(VariableType.PASSWORD.getTypeName()),
                description = stringResource(VariableType.PASSWORD.getTypeDescription()),
                onClick = {
                    onVariableTypeSelected(VariableType.PASSWORD)
                },
            )
            SelectDialogEntry(
                label = stringResource(VariableType.DATE.getTypeName()),
                description = stringResource(VariableType.DATE.getTypeDescription()),
                onClick = {
                    onVariableTypeSelected(VariableType.DATE)
                },
            )
            SelectDialogEntry(
                label = stringResource(VariableType.TIME.getTypeName()),
                description = stringResource(VariableType.TIME.getTypeDescription()),
                onClick = {
                    onVariableTypeSelected(VariableType.TIME)
                },
            )
            SelectDialogEntry(
                label = stringResource(VariableType.COLOR.getTypeName()),
                description = stringResource(VariableType.COLOR.getTypeDescription()),
                onClick = {
                    onVariableTypeSelected(VariableType.COLOR)
                },
            )
            Divider()
            SelectDialogEntry(
                label = stringResource(VariableType.TOGGLE.getTypeName()),
                description = stringResource(VariableType.TOGGLE.getTypeDescription()),
                onClick = {
                    onVariableTypeSelected(VariableType.TOGGLE)
                },
            )
            SelectDialogEntry(
                label = stringResource(VariableType.CLIPBOARD.getTypeName()),
                description = stringResource(VariableType.CLIPBOARD.getTypeDescription()),
                onClick = {
                    onVariableTypeSelected(VariableType.CLIPBOARD)
                },
            )
            SelectDialogEntry(
                label = stringResource(VariableType.UUID.getTypeName()),
                description = stringResource(VariableType.UUID.getTypeDescription()),
                onClick = {
                    onVariableTypeSelected(VariableType.UUID)
                },
            )
        }
    }
}

@Composable
private fun DeletionDialog(
    shortcutNames: List<String>,
    onConfirmed: () -> Unit,
    onDismissed: () -> Unit,
) {
    ConfirmDialog(
        message = if (shortcutNames.isEmpty()) {
            stringResource(R.string.confirm_delete_variable_message)
        } else {
            stringResource(R.string.confirm_delete_variable_message)
                .plus("\n\n")
                .plus(
                    pluralStringResource(
                        R.plurals.warning_variable_still_in_use_in_shortcuts, shortcutNames.size,
                        shortcutNames.joinToString(),
                        shortcutNames.size,
                    )
                )
        },
        confirmButton = stringResource(R.string.dialog_delete),
        onConfirmRequest = onConfirmed,
        onDismissRequest = onDismissed,
    )
}

package ch.rmy.android.http_shortcuts.activities.variables

import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
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
                title = dialogState.variableKey,
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

@Composable
private fun CreationDialog(
    onVariableTypeSelected: (VariableType) -> Unit,
    onDismissed: () -> Unit,
) {
    SelectDialog(
        title = stringResource(R.string.title_select_variable_type),
        onDismissRequest = onDismissed,
    ) {
        VariableTypeEntry(VariableType.CONSTANT, onVariableTypeSelected)
        HorizontalDivider()
        VariableTypeEntry(VariableType.SELECT, onVariableTypeSelected)
        VariableTypeEntry(VariableType.TEXT, onVariableTypeSelected)
        VariableTypeEntry(VariableType.NUMBER, onVariableTypeSelected)
        VariableTypeEntry(VariableType.SLIDER, onVariableTypeSelected)
        VariableTypeEntry(VariableType.PASSWORD, onVariableTypeSelected)
        VariableTypeEntry(VariableType.DATE, onVariableTypeSelected)
        VariableTypeEntry(VariableType.TIME, onVariableTypeSelected)
        VariableTypeEntry(VariableType.COLOR, onVariableTypeSelected)
        HorizontalDivider()
        VariableTypeEntry(VariableType.TOGGLE, onVariableTypeSelected)
        VariableTypeEntry(VariableType.INCREMENT, onVariableTypeSelected)
        VariableTypeEntry(VariableType.CLIPBOARD, onVariableTypeSelected)
        VariableTypeEntry(VariableType.TIMESTAMP, onVariableTypeSelected)
        VariableTypeEntry(VariableType.UUID, onVariableTypeSelected)
    }
}

@Composable
private fun VariableTypeEntry(
    variableType: VariableType,
    onVariableTypeSelected: (VariableType) -> Unit,
) {
    SelectDialogEntry(
        label = stringResource(variableType.getTypeName()),
        description = stringResource(variableType.getTypeDescription()),
        onClick = {
            onVariableTypeSelected(variableType)
        },
    )
}

@Composable
private fun DeletionDialog(
    title: String,
    shortcutNames: List<String>,
    onConfirmed: () -> Unit,
    onDismissed: () -> Unit,
) {
    ConfirmDialog(
        title = title,
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

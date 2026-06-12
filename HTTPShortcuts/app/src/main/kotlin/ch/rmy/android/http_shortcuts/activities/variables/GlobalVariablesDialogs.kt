package ch.rmy.android.http_shortcuts.activities.variables

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.variables.VariableTypeMappings.getTypeDescription
import ch.rmy.android.http_shortcuts.activities.variables.VariableTypeMappings.getTypeName
import ch.rmy.android.http_shortcuts.components.ConfirmDialog
import ch.rmy.android.http_shortcuts.components.SelectDialog
import ch.rmy.android.http_shortcuts.components.SelectDialogEntry
import ch.rmy.android.http_shortcuts.data.enums.VariableType

@Composable
fun GlobalVariablesDialogs(
    dialogState: GlobalVariablesDialogState?,
    onUseClicked: () -> Unit,
    onVariableTypeSelected: (VariableType) -> Unit,
    onEditClicked: () -> Unit,
    onDuplicateClicked: () -> Unit,
    onDeleteClicked: () -> Unit,
    onDeleteConfirmed: () -> Unit,
    onDismissed: () -> Unit,
) {
    when (dialogState) {
        is GlobalVariablesDialogState.ContextMenu -> {
            ContextMenuDialog(
                title = dialogState.variableKey,
                showUse = dialogState.showUse,
                onUseClicked = onUseClicked,
                onEditClicked = onEditClicked,
                onDuplicateClicked = onDuplicateClicked,
                onDeleteClicked = onDeleteClicked,
                onDismissed = onDismissed,
            )
        }
        is GlobalVariablesDialogState.Creation -> {
            CreationDialog(
                onVariableTypeSelected = onVariableTypeSelected,
                onDismissed = onDismissed,
            )
        }
        is GlobalVariablesDialogState.Delete -> {
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
    showUse: Boolean,
    onUseClicked: () -> Unit,
    onEditClicked: () -> Unit,
    onDuplicateClicked: () -> Unit,
    onDeleteClicked: () -> Unit,
    onDismissed: () -> Unit,
) {
    SelectDialog(
        title = title,
        onDismissRequest = onDismissed,
    ) { horizontalPadding ->
        if (showUse) {
            SelectDialogEntry(
                horizontalPadding = horizontalPadding,
                label = stringResource(R.string.action_select),
                icon = painterResource(R.drawable.outline_check_24),
                onClick = onUseClicked,
            )
        }
        SelectDialogEntry(
            horizontalPadding = horizontalPadding,
            label = stringResource(R.string.action_edit),
            icon = painterResource(R.drawable.outline_edit_24),
            onClick = onEditClicked,
        )
        SelectDialogEntry(
            horizontalPadding = horizontalPadding,
            label = stringResource(R.string.action_duplicate),
            icon = painterResource(R.drawable.outline_file_copy_24),
            onClick = onDuplicateClicked,
        )
        SelectDialogEntry(
            horizontalPadding = horizontalPadding,
            label = stringResource(R.string.action_delete),
            icon = painterResource(R.drawable.outline_delete_24),
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
    ) { horizontalPadding ->
        VariableTypeEntry(horizontalPadding, VariableType.CONSTANT, onVariableTypeSelected)
        HorizontalDivider(modifier = Modifier.padding(horizontal = horizontalPadding))
        VariableTypeEntry(horizontalPadding, VariableType.SELECT, onVariableTypeSelected)
        VariableTypeEntry(horizontalPadding, VariableType.TEXT, onVariableTypeSelected)
        VariableTypeEntry(horizontalPadding, VariableType.NUMBER, onVariableTypeSelected)
        VariableTypeEntry(horizontalPadding, VariableType.SLIDER, onVariableTypeSelected)
        VariableTypeEntry(horizontalPadding, VariableType.PASSWORD, onVariableTypeSelected)
        VariableTypeEntry(horizontalPadding, VariableType.DATE, onVariableTypeSelected)
        VariableTypeEntry(horizontalPadding, VariableType.TIME, onVariableTypeSelected)
        VariableTypeEntry(horizontalPadding, VariableType.COLOR, onVariableTypeSelected)
        HorizontalDivider(modifier = Modifier.padding(horizontal = horizontalPadding))
        VariableTypeEntry(horizontalPadding, VariableType.TOGGLE, onVariableTypeSelected)
        VariableTypeEntry(horizontalPadding, VariableType.INCREMENT, onVariableTypeSelected)
        VariableTypeEntry(horizontalPadding, VariableType.CLIPBOARD, onVariableTypeSelected)
        VariableTypeEntry(horizontalPadding, VariableType.TIMESTAMP, onVariableTypeSelected)
        VariableTypeEntry(horizontalPadding, VariableType.UUID, onVariableTypeSelected)
    }
}

@Composable
private fun VariableTypeEntry(
    horizontalPadding: Dp,
    variableType: VariableType,
    onVariableTypeSelected: (VariableType) -> Unit,
) {
    SelectDialogEntry(
        horizontalPadding = horizontalPadding,
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
                        R.plurals.warning_variable_still_in_use_in_shortcuts,
                        shortcutNames.size,
                        shortcutNames.joinToString(),
                        shortcutNames.size,
                    ),
                )
        },
        confirmButton = stringResource(R.string.dialog_delete),
        onConfirmRequest = onConfirmed,
        onDismissRequest = onDismissed,
    )
}

package ch.rmy.android.http_shortcuts.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ch.rmy.android.framework.viewmodel.ViewModelEvent
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.variables.VariableTypeMappings.getTypeName
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableId
import ch.rmy.android.http_shortcuts.data.dtos.VariablePlaceholder
import ch.rmy.android.http_shortcuts.navigation.NavigationDestination

@Composable
fun VariablePickerDialog(
    title: String,
    variables: List<VariablePlaceholder>,
    showEditButton: Boolean = true,
    onVariableSelected: (VariableId) -> Unit,
    onDismissRequested: () -> Unit,
) {
    val eventHandler = LocalEventinator.current
    val onEditVariablesClicked = {
        onDismissRequested()
        eventHandler.onEvent(ViewModelEvent.Navigate(NavigationDestination.Variables))
    }

    if (variables.isEmpty()) {
        AlertDialog(
            onDismissRequest = onDismissRequested,
            text = {
                Text(
                    stringResource(
                        if (showEditButton) {
                            R.string.help_text_variable_button
                        } else {
                            R.string.help_text_variable_button_for_variables
                        }
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = onDismissRequested) {
                    Text(stringResource(R.string.dialog_ok))
                }
            },
            dismissButton = if (showEditButton) {
                {
                    TextButton(onClick = onEditVariablesClicked) {
                        Text(stringResource(R.string.button_create_first_variable))
                    }
                }
            } else null,
        )
        return
    }

    SelectDialog(
        title = title,
        scrolling = false,
        onDismissRequest = onDismissRequested,
        extraButton = if (showEditButton) {
            {
                TextButton(onClick = onEditVariablesClicked) {
                    Text(stringResource(R.string.label_edit_variables))
                }
            }
        } else null
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            items(
                items = variables,
                key = { it.variableId },
            ) { variable ->
                SelectDialogEntry(
                    label = variable.variableKey,
                    description = stringResource(variable.variableType.getTypeName()),
                    onClick = {
                        onVariableSelected(variable.variableId)
                    }
                )
            }
        }
    }
}

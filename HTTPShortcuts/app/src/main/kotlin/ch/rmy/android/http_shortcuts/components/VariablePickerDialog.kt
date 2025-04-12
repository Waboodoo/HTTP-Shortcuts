package ch.rmy.android.http_shortcuts.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.SavedStateHandle
import ch.rmy.android.framework.viewmodel.ViewModelEvent
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.variables.VariableTypeMappings.getTypeName
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableId
import ch.rmy.android.http_shortcuts.data.dtos.VariablePlaceholder
import ch.rmy.android.http_shortcuts.navigation.NavigationDestination
import ch.rmy.android.http_shortcuts.navigation.ResultHandler

@Composable
fun VariablePickerDialog(
    savedStateHandle: SavedStateHandle,
    title: String,
    variables: List<VariablePlaceholder>,
    showEditButton: Boolean = true,
    onVariableSelected: (VariableId) -> Unit,
    onDismissRequested: () -> Unit,
) {
    val eventHandler = LocalEventinator.current

    var pickerOpened by rememberSaveable {
        mutableStateOf(false)
    }
    if (pickerOpened) {
        ResultHandler(savedStateHandle) { result ->
            if (result is NavigationDestination.Variables.VariableSelectedResult) {
                onVariableSelected(result.variableId)
                onDismissRequested()
                pickerOpened = false
            }
        }
    }

    val onEditVariablesClicked = {
        pickerOpened = true
        eventHandler.onEvent(ViewModelEvent.Navigate(NavigationDestination.Variables.buildRequest(asPicker = true)))
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
                        },
                    ),
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
            } else {
                null
            },
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
        } else {
            null
        },
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth(),
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
                    },
                )
            }
        }
    }
}

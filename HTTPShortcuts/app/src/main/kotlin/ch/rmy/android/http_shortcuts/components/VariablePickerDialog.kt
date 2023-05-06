package ch.rmy.android.http_shortcuts.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import ch.rmy.android.framework.extensions.startActivity
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.variables.VariableTypeMappings.getTypeName
import ch.rmy.android.http_shortcuts.activities.variables.VariablesActivity
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableId
import ch.rmy.android.http_shortcuts.data.dtos.VariablePlaceholder

@Composable
fun VariablePickerDialog(
    title: String,
    variables: List<VariablePlaceholder>,
    showEditButton: Boolean = true,
    onVariableSelected: (VariableId) -> Unit,
    onDismissRequested: () -> Unit,
) {
    val context = LocalContext.current
    val openVariableEditor = {
        VariablesActivity.IntentBuilder()
            .startActivity(context)
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
                    TextButton(onClick = openVariableEditor) {
                        Text(stringResource(R.string.button_create_first_variable))
                    }
                }
            } else null,
        )
        return
    }

    SelectDialog(
        title = title,
        onDismissRequest = onDismissRequested,
        extraButton = if (showEditButton) {
            {
                TextButton(onClick = openVariableEditor) {
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
                key = { it.variableKey },
            ) { variable ->
                MenuDialogEntry(
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

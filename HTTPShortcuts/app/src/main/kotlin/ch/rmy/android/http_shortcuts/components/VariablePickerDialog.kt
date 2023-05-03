package ch.rmy.android.http_shortcuts.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ch.rmy.android.http_shortcuts.activities.variables.VariableTypeMappings.getTypeName
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableId
import ch.rmy.android.http_shortcuts.data.dtos.VariablePlaceholder

@Composable
fun VariablePickerDialog(
    title: String,
    variables: List<VariablePlaceholder>,
    onVariableSelected: (VariableId) -> Unit,
    onDismissRequested: () -> Unit,
) {
    SelectDialog(
        title = title,
        onDismissRequest = onDismissRequested,
    ) {
        // TODO: Empty state

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

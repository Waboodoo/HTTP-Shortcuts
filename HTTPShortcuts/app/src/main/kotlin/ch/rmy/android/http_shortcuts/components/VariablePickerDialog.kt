package ch.rmy.android.http_shortcuts.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ch.rmy.android.http_shortcuts.activities.variables.VariableTypeMappings
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
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            itemsIndexed(
                items = variables,
                key = { _, it -> it.variableKey },
            ) { index, variable ->
                if (index != 0) {
                    Divider()
                }
                MenuDialogEntry(
                    label = variable.variableKey,
                    description = stringResource(VariableTypeMappings.getTypeName(variable.variableType)),
                    onClick = {
                        onVariableSelected(variable.variableId)
                    }
                )
            }
        }
    }
}

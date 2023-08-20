package ch.rmy.android.http_shortcuts.activities.variables

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.variables.models.VariableListItem
import ch.rmy.android.http_shortcuts.components.EmptyState
import ch.rmy.android.http_shortcuts.components.Spacing
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableId
import ch.rmy.android.http_shortcuts.extensions.localize
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

@Composable
fun VariablesContent(
    variables: List<VariableListItem>,
    onVariableClicked: (VariableId) -> Unit,
    onVariableMoved: (VariableId, VariableId) -> Unit,
) {
    if (variables.isEmpty()) {
        EmptyState(
            title = stringResource(R.string.empty_state_variables),
            description = stringResource(R.string.empty_state_variables_instructions),
        )
        return
    }

    val reorderableState = rememberReorderableLazyListState(
        onMove = { from, to ->
            onVariableMoved(from.key as VariableId, to.key as VariableId)
        },
    )

    LazyColumn(
        state = reorderableState.listState,
        modifier = Modifier
            .fillMaxSize()
            .reorderable(reorderableState)
            .detectReorderAfterLongPress(reorderableState),
    ) {
        items(
            items = variables,
            contentType = { "variable" },
            key = { it.id },
        ) { item ->
            ReorderableItem(reorderableState, key = item.id) { isDragging ->
                val elevation = animateDpAsState(if (isDragging) 16.dp else 0.dp)
                VariableItem(
                    variable = item,
                    modifier = Modifier
                        .shadow(elevation.value)
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable {
                            onVariableClicked(item.id)
                        },
                )
            }
        }

        item(
            key = "spacer",
            contentType = "spacer",
        ) {
            Spacer(modifier = Modifier.height(Spacing.HUGE))
        }
    }
}

@Composable
private fun VariableItem(
    variable: VariableListItem,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier,
    ) {
        ListItem(
            modifier = Modifier
                .fillMaxWidth(),
            headlineContent = {
                Text(variable.key)
            },
            supportingContent = {
                Text(variable.type.localize())
            },
            trailingContent = {
                if (variable.isUnused) {
                    Text(stringResource(R.string.label_variable_unused))
                }
            }
        )
        HorizontalDivider()
    }
}

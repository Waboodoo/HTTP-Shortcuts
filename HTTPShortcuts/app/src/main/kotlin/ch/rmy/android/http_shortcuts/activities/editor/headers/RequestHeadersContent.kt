package ch.rmy.android.http_shortcuts.activities.editor.headers

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.editor.headers.models.HeaderListItem
import ch.rmy.android.http_shortcuts.components.EmptyState
import ch.rmy.android.http_shortcuts.components.VariablePlaceholderText
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

@Composable
fun RequestHeadersContent(
    headers: List<HeaderListItem>,
    onHeaderClicked: (String) -> Unit,
    onHeaderMoved: (String, String) -> Unit,
) {
    if (headers.isEmpty()) {
        EmptyState(
            title = stringResource(R.string.empty_state_request_headers),
            description = stringResource(R.string.empty_state_request_headers_instructions),
        )
        return
    }

    val reorderableState = rememberReorderableLazyListState(
        onMove = { from, to ->
            onHeaderMoved(from.key as String, to.key as String)
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
            items = headers,
            key = { it.id },
        ) { item ->
            ReorderableItem(reorderableState, key = item.id) { isDragging ->
                val elevation = animateDpAsState(if (isDragging) 16.dp else 0.dp)
                HeaderItem(
                    header = item,
                    modifier = Modifier
                        .shadow(elevation.value)
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable {
                            onHeaderClicked(item.id)
                        },
                )
            }
        }
    }
}

@Composable
private fun HeaderItem(
    header: HeaderListItem,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier,
    ) {
        ListItem(
            modifier = Modifier.fillMaxWidth(),
            headlineContent = {
                VariablePlaceholderText(header.key, maxLines = 2, overflow = TextOverflow.Ellipsis)
            },
            supportingContent = {
                VariablePlaceholderText(header.value, maxLines = 2, overflow = TextOverflow.Ellipsis)
            },
        )
        HorizontalDivider()
    }
}

package ch.rmy.android.http_shortcuts.activities.editor.headers

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ch.rmy.android.framework.extensions.move
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.editor.headers.models.HeaderListItem
import ch.rmy.android.http_shortcuts.components.EmptyState
import ch.rmy.android.http_shortcuts.components.VariablePlaceholderText
import ch.rmy.android.http_shortcuts.data.domains.request_headers.RequestHeaderId
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RequestHeadersContent(
    headers: List<HeaderListItem>,
    onHeaderClicked: (RequestHeaderId) -> Unit,
    onHeaderMoved: (RequestHeaderId, RequestHeaderId) -> Unit,
) {
    if (headers.isEmpty()) {
        EmptyState(
            title = stringResource(R.string.empty_state_request_headers),
            description = stringResource(R.string.empty_state_request_headers_instructions),
        )
        return
    }

    var localHeaders by remember(headers) { mutableStateOf(headers) }
    val lazyListState = rememberLazyListState()
    val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
        localHeaders = localHeaders.move(from.index, to.index)
        onHeaderMoved(from.key as RequestHeaderId, to.key as RequestHeaderId)
    }

    LazyColumn(
        state = lazyListState,
        modifier = Modifier
            .fillMaxSize(),
    ) {
        items(
            items = localHeaders,
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
                        }
                        .longPressDraggableHandle(),
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

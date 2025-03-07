package ch.rmy.android.http_shortcuts.activities.editor.shortcuts

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
import androidx.compose.material3.Text
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
import ch.rmy.android.http_shortcuts.activities.editor.shortcuts.models.ShortcutListItem
import ch.rmy.android.http_shortcuts.activities.editor.shortcuts.models.ShortcutListItemId
import ch.rmy.android.http_shortcuts.components.EmptyState
import ch.rmy.android.http_shortcuts.components.ShortcutIcon
import ch.rmy.android.http_shortcuts.extensions.localize
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TriggerShortcutsContent(
    shortcuts: List<ShortcutListItem>,
    onShortcutClicked: (ShortcutListItemId) -> Unit,
    onShortcutMoved: (ShortcutListItemId, ShortcutListItemId) -> Unit,
) {
    if (shortcuts.isEmpty()) {
        EmptyState(
            title = stringResource(R.string.empty_state_trigger_shortcuts),
            description = stringResource(R.string.empty_state_trigger_shortcuts_instructions),
        )
        return
    }

    var localShortcuts by remember(shortcuts) { mutableStateOf(shortcuts) }
    val lazyListState = rememberLazyListState()
    val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
        localShortcuts = localShortcuts.move(from.index, to.index)
        onShortcutMoved(
            ShortcutListItemId.fromString(from.key as String),
            ShortcutListItemId.fromString(to.key as String),
        )
    }

    LazyColumn(
        state = lazyListState,
        modifier = Modifier
            .fillMaxSize(),
    ) {
        items(
            items = localShortcuts,
            key = { it.id.toString() },
        ) { item ->
            ReorderableItem(reorderableState, key = item.id.toString()) { isDragging ->
                val elevation = animateDpAsState(if (isDragging) 16.dp else 0.dp)
                ShortcutItem(
                    shortcut = item,
                    modifier = Modifier
                        .shadow(elevation.value)
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable {
                            onShortcutClicked(item.id)
                        }
                        .longPressDraggableHandle(),
                )
            }
        }
    }
}

@Composable
private fun ShortcutItem(
    shortcut: ShortcutListItem,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier,
    ) {
        ListItem(
            modifier = Modifier.fillMaxWidth(),
            headlineContent = {
                Text(shortcut.name.localize(), maxLines = 2, overflow = TextOverflow.Ellipsis)
            },
            leadingContent = {
                ShortcutIcon(shortcut.icon)
            },
        )
        HorizontalDivider()
    }
}

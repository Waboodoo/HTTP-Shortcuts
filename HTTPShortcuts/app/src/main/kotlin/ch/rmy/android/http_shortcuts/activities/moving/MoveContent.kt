package ch.rmy.android.http_shortcuts.activities.moving

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ch.rmy.android.framework.extensions.logInfo
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.moving.models.CategoryItem
import ch.rmy.android.http_shortcuts.components.FontSize
import ch.rmy.android.http_shortcuts.components.ScreenInstructionsHeaders
import ch.rmy.android.http_shortcuts.components.ShortcutIcon
import ch.rmy.android.http_shortcuts.components.Spacing
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.dtos.ShortcutPlaceholder
import kotlinx.coroutines.delay
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MoveContent(
    categories: List<CategoryItem>,
    initialShortcut: ShortcutId,
    onShortcutMovedToShortcut: (ShortcutId, ShortcutId) -> Unit,
    onShortcutMovedToCategory: (ShortcutId, CategoryId) -> Unit,
    onMoveEnded: () -> Unit,
) {
    var scrolledToInitial by rememberSaveable {
        mutableStateOf(false)
    }

    val lazyListState = rememberLazyListState()
    val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
        val shortcutId = from.key as ShortcutId
        val targetKey = to.key as String
        logInfo("MoveContent", "Moving shortcuts, from=$shortcutId, to=$targetKey")
        if (targetKey.startsWith(CATEGORY_KEY_PREFIX)) {
            onShortcutMovedToCategory(shortcutId, targetKey.removePrefix(CATEGORY_KEY_PREFIX))
        } else {
            onShortcutMovedToShortcut(shortcutId, targetKey)
        }
    }

    if (!scrolledToInitial) {
        LaunchedEffect(Unit) {
            delay(300.milliseconds)
            categories.findIndexOf(initialShortcut)
                ?.takeIf { it > 5 }
                ?.let { index ->
                    lazyListState.animateScrollToItem(index - 3)
                }
            scrolledToInitial = true
        }
    }

    Column {
        ScreenInstructionsHeaders(text = stringResource(R.string.message_moving_enabled))
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(
                    vertical = Spacing.MEDIUM,
                ),
            verticalArrangement = Arrangement.spacedBy(Spacing.SMALL),
        ) {
            categories.forEachIndexed { index, category ->
                item(
                    key = "$CATEGORY_KEY_PREFIX${category.id}",
                    contentType = "category",
                ) {
                    ReorderableItem(
                        reorderableState,
                        key = "$CATEGORY_KEY_PREFIX${category.id}",
                        enabled = true,
                    ) {
                        Column {
                            if (index != 0) {
                                Spacer(
                                    modifier = Modifier.height(Spacing.MEDIUM),
                                )
                            }
                            CategoryHeader(
                                name = category.name,
                            )

                            if (category.shortcuts.isEmpty()) {
                                EmptyCategoryContent()
                            }
                        }
                    }
                }

                items(
                    items = category.shortcuts,
                    key = { it.id },
                    contentType = { "shortcut" },
                ) { item ->
                    ReorderableItem(
                        reorderableState,
                        key = item.id,
                    ) { isDragging ->
                        val elevation = animateDpAsState(if (isDragging) 16.dp else 0.dp)
                        ShortcutListItem(
                            modifier = Modifier
                                .shadow(elevation.value)
                                .background(MaterialTheme.colorScheme.surface)
                                .longPressDraggableHandle(
                                    onDragStopped = onMoveEnded,
                                ),
                            shortcut = item,
                        )
                    }
                }
            }
        }
    }
}

private fun List<CategoryItem>.findIndexOf(shortcutId: ShortcutId): Int? {
    var i = 0
    for (category in this) {
        i++
        for (shortcut in category.shortcuts) {
            if (shortcut.id == shortcutId) {
                return i
            }
            i++
        }
    }
    return null
}

@Composable
private fun CategoryHeader(
    name: String,
) {
    Text(
        modifier = Modifier
            .padding(horizontal = Spacing.MEDIUM),
        text = name,
        fontSize = FontSize.BIG,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
private fun ShortcutListItem(
    modifier: Modifier,
    shortcut: ShortcutPlaceholder,
) {
    Column(
        modifier,
    ) {
        ListItem(
            modifier = Modifier.fillMaxWidth(),
            headlineContent = {
                Text(
                    text = shortcut.name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            leadingContent = {
                ShortcutIcon(shortcut.icon)
            },
        )
        HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.3f))
    }
}

@Composable
private fun EmptyCategoryContent() {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        ListItem(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(0.8f),
            headlineContent = {
                Text(
                    text = stringResource(R.string.placeholder_empty_category_for_moving),
                    fontStyle = FontStyle.Italic,
                    maxLines = 2,
                )
            },
        )
        HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.3f))
    }
}

private const val CATEGORY_KEY_PREFIX = "category-"

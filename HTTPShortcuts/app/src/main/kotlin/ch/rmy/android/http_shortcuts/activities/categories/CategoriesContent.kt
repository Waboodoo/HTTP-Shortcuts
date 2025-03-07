package ch.rmy.android.http_shortcuts.activities.categories

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import ch.rmy.android.framework.extensions.move
import ch.rmy.android.http_shortcuts.activities.categories.models.CategoryListItem
import ch.rmy.android.http_shortcuts.components.ShortcutIcon
import ch.rmy.android.http_shortcuts.components.Spacing
import ch.rmy.android.http_shortcuts.components.VerticalSpacer
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId
import ch.rmy.android.http_shortcuts.data.enums.CategoryLayoutType
import ch.rmy.android.http_shortcuts.extensions.localize
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CategoriesContent(
    categories: List<CategoryListItem>,
    onCategoryClicked: (CategoryId) -> Unit,
    onCategoryMoved: (CategoryId, CategoryId) -> Unit,
) {
    var localCategories by remember(categories) { mutableStateOf(categories) }
    val lazyListState = rememberLazyListState()
    val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
        localCategories = localCategories.move(from.index, to.index)
        onCategoryMoved(from.key as CategoryId, to.key as CategoryId)
    }

    LazyColumn(
        state = lazyListState,
        modifier = Modifier
            .fillMaxSize(),
    ) {
        items(
            items = localCategories,
            contentType = { "category" },
            key = { it.id },
        ) { item ->
            ReorderableItem(reorderableState, key = item.id) { isDragging ->
                val elevation = animateDpAsState(if (isDragging) 16.dp else 0.dp)
                CategoryItem(
                    category = item,
                    modifier = Modifier
                        .shadow(elevation.value)
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable {
                            onCategoryClicked(item.id)
                        }
                        .longPressDraggableHandle(),
                )
            }
        }

        item(
            key = "spacer",
            contentType = "spacer",
        ) {
            VerticalSpacer(Spacing.HUGE)
        }
    }
}

@Composable
private fun CategoryItem(
    category: CategoryListItem,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier,
    ) {
        ListItem(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(if (category.hidden) 0.6f else 1f),
            headlineContent = {
                Text(category.name.localize())
            },
            supportingContent = {
                Text(category.description.localize())
            },
            trailingContent = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(Spacing.TINY),
                    horizontalAlignment = Alignment.End,
                ) {
                    val typeIcon = when (category.layoutType) {
                        CategoryLayoutType.LINEAR_LIST -> Icons.AutoMirrored.Filled.List
                        CategoryLayoutType.DENSE_GRID,
                        CategoryLayoutType.MEDIUM_GRID,
                        CategoryLayoutType.WIDE_GRID,
                        -> Icons.Filled.GridView

                        null -> null
                    }
                    if (typeIcon != null) {
                        Icon(
                            typeIcon,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.End,
                    ) {
                        category.icons.forEachIndexed { index, icon ->
                            ShortcutIcon(
                                icon,
                                size = 20.dp,
                                modifier = Modifier
                                    .offset(x = 5.dp * (category.icons.lastIndex - index))
                                    .zIndex((category.icons.lastIndex - index).toFloat()),
                            )
                        }
                    }
                }
            },
        )
        HorizontalDivider()
    }
}

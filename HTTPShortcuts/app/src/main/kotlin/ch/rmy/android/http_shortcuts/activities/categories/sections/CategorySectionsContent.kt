package ch.rmy.android.http_shortcuts.activities.categories.sections

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
import ch.rmy.android.http_shortcuts.activities.categories.sections.models.CategorySectionListItem
import ch.rmy.android.http_shortcuts.components.EmptyState
import ch.rmy.android.http_shortcuts.data.domains.sections.SectionId
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CategorySectionsContent(
    sections: List<CategorySectionListItem>,
    onSectionClicked: (SectionId) -> Unit,
    onSectionMoved: (SectionId, SectionId) -> Unit,
) {
    if (sections.isEmpty()) {
        EmptyState(
            title = stringResource(R.string.empty_state_category_sections),
            description = stringResource(R.string.empty_state_category_sections_instructions),
        )
        return
    }

    var localSections by remember(sections) { mutableStateOf(sections) }
    val lazyListState = rememberLazyListState()
    val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
        localSections = localSections.move(from.index, to.index)
        onSectionMoved(from.key as SectionId, to.key as SectionId)
    }

    LazyColumn(
        state = lazyListState,
        modifier = Modifier
            .fillMaxSize(),
    ) {
        items(
            items = sections,
            key = { it.id },
        ) { item ->
            ReorderableItem(reorderableState, key = item.id) { isDragging ->
                val elevation = animateDpAsState(if (isDragging) 16.dp else 0.dp)
                SectionItem(
                    section = item,
                    modifier = Modifier
                        .shadow(elevation.value)
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable {
                            onSectionClicked(item.id)
                        }
                        .longPressDraggableHandle(),
                )
            }
        }
    }
}

@Composable
private fun SectionItem(
    section: CategorySectionListItem,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier,
    ) {
        ListItem(
            modifier = Modifier.fillMaxWidth(),
            headlineContent = {
                Text(section.name, maxLines = 2, overflow = TextOverflow.Ellipsis)
            },
        )
        HorizontalDivider()
    }
}

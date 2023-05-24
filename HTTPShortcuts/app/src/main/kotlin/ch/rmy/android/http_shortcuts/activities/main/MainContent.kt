package ch.rmy.android.http_shortcuts.activities.main

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import ch.rmy.android.framework.extensions.indexOfFirstOrNull
import ch.rmy.android.http_shortcuts.activities.main.models.CategoryItem
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.dtos.ShortcutPlaceholder
import ch.rmy.android.http_shortcuts.data.enums.SelectionMode

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainContent(
    categoryItems: List<CategoryItem>,
    selectionMode: SelectionMode,
    activeCategoryId: CategoryId,
    onActiveCategoryIdChanged: (CategoryId) -> Unit,
    onShortcutEdited: () -> Unit,
    onPlaceShortcutOnHomeScreen: (ShortcutPlaceholder) -> Unit,
    onRemoveShortcutFromHomeScreen: (ShortcutPlaceholder) -> Unit,
    onSelectShortcut: (ShortcutId) -> Unit,
) {
    val activeTabIndex = categoryItems.indexOfFirstOrNull { it.categoryId == activeCategoryId } ?: 0
    val pagerState = rememberPagerState(initialPage = activeTabIndex) {
        categoryItems.size
    }

    LaunchedEffect(activeCategoryId) {
        pagerState.animateScrollToPage(categoryItems.indexOfFirstOrNull { it.categoryId == activeCategoryId } ?: 0)
    }

    LaunchedEffect(pagerState.settledPage) {
        onActiveCategoryIdChanged(categoryItems[pagerState.settledPage].categoryId)
    }
    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        if (categoryItems.size > 1) {
            ScrollableTabRow(selectedTabIndex = activeTabIndex) {
                categoryItems.forEachIndexed { index, category ->
                    Tab(
                        selected = index == activeTabIndex,
                        onClick = {
                            onActiveCategoryIdChanged(category.categoryId)
                        },
                        text = {
                            Text(category.name)
                        }
                    )
                }
            }
        }
        HorizontalPager(
            state = pagerState,
            userScrollEnabled = true,
            beyondBoundsPageCount = 2,
            key = { index ->
                categoryItems[index].categoryId
            },
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
        ) { index ->
            ShortcutListContent(
                category = categoryItems[index],
                selectionMode = selectionMode,
                isActive = index == activeTabIndex,
                onShortcutEdited = onShortcutEdited,
                onPlaceShortcutOnHomeScreen = onPlaceShortcutOnHomeScreen,
                onRemoveShortcutFromHomeScreen = onRemoveShortcutFromHomeScreen,
                onSelectShortcut = onSelectShortcut,
            )
        }
    }
}

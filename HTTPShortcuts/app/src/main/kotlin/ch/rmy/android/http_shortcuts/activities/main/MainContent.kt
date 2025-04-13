package ch.rmy.android.http_shortcuts.activities.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.rmy.android.framework.extensions.indexOfFirstOrNull
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.main.models.CategoryItem
import ch.rmy.android.http_shortcuts.components.ScreenInstructionsHeaders
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.dtos.ShortcutPlaceholder
import ch.rmy.android.http_shortcuts.data.enums.SelectionMode
import kotlinx.coroutines.CancellationException

@Composable
fun MainContent(
    categoryItems: List<CategoryItem>,
    hasMultipleCategories: Boolean,
    selectionMode: SelectionMode,
    activeCategoryId: CategoryId,
    onActiveCategoryIdChanged: (CategoryId) -> Unit,
    onPlaceShortcutOnHomeScreen: (ShortcutPlaceholder) -> Unit,
    onRemoveShortcutFromHomeScreen: (ShortcutPlaceholder) -> Unit,
    onSelectShortcut: (ShortcutId) -> Unit,
) {
    val pagerState = rememberPagerState(
        initialPage = categoryItems.indexOfFirstOrNull { it.categoryId == activeCategoryId } ?: 0,
    ) {
        categoryItems.size
    }

    LaunchedEffect(activeCategoryId) {
        try {
            pagerState.animateScrollToPage(categoryItems.indexOfFirstOrNull { it.categoryId == activeCategoryId } ?: 0)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logException(e)
        }
    }

    LaunchedEffect(pagerState.settledPage) {
        onActiveCategoryIdChanged(categoryItems[pagerState.settledPage].categoryId)
    }
    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        if (categoryItems.size > 1) {
            TabBar(
                categoryItems = categoryItems,
                activeTabIndex = pagerState.currentPage.coerceAtMost(categoryItems.size - 1),
                onActiveCategoryIdChanged = onActiveCategoryIdChanged,
            )
        }

        when (selectionMode) {
            SelectionMode.HOME_SCREEN_SHORTCUT_PLACEMENT, SelectionMode.HOME_SCREEN_WIDGET_PLACEMENT -> {
                ScreenInstructionsHeaders(stringResource(R.string.instructions_select_shortcut_for_home_screen))
            }
            SelectionMode.PLUGIN -> {
                ScreenInstructionsHeaders(stringResource(R.string.instructions_select_shortcut_for_plugin))
            }
            else -> Unit
        }

        HorizontalPager(
            state = pagerState,
            userScrollEnabled = true,
            beyondViewportPageCount = 2,
            flingBehavior = PagerDefaults.flingBehavior(state = pagerState, snapPositionalThreshold = 0.3f),
            key = { index ->
                categoryItems[index].categoryId
            },
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
        ) { index ->
            ShortcutListContent(
                category = categoryItems[index],
                hasMultipleCategories = hasMultipleCategories,
                selectionMode = selectionMode,
                isActive = index == pagerState.currentPage,
                onPlaceShortcutOnHomeScreen = onPlaceShortcutOnHomeScreen,
                onRemoveShortcutFromHomeScreen = onRemoveShortcutFromHomeScreen,
                onSelectShortcut = onSelectShortcut,
            )
        }
    }
}

@Composable
private fun TabBar(
    categoryItems: List<CategoryItem>,
    activeTabIndex: Int,
    onActiveCategoryIdChanged: (CategoryId) -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter,
    ) {
        ScrollableTabRow(
            selectedTabIndex = activeTabIndex,
            modifier = Modifier.fillMaxWidth(),
            edgePadding = 16.dp,
            divider = {},
        ) {
            categoryItems.forEachIndexed { index, category ->
                Tab(
                    selected = index == activeTabIndex,
                    onClick = {
                        onActiveCategoryIdChanged(category.categoryId)
                    },
                    text = {
                        Text(category.name)
                    },
                )
            }
        }

        HorizontalDivider()
    }
}

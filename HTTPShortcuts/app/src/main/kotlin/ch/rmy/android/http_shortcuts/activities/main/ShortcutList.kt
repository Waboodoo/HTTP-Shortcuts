package ch.rmy.android.http_shortcuts.activities.main

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ch.rmy.android.framework.extensions.runIf
import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.main.models.ShortcutListItem as ShortcutListItemModel
import ch.rmy.android.http_shortcuts.components.DefaultTextShadow
import ch.rmy.android.http_shortcuts.components.EmptyState
import ch.rmy.android.http_shortcuts.components.FontSize
import ch.rmy.android.http_shortcuts.components.ShortcutIcon
import ch.rmy.android.http_shortcuts.components.Spacing
import ch.rmy.android.http_shortcuts.components.VerticalSpacer
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.enums.CategoryLayoutType

private const val HIDDEN_ALPHA = 0.4f

@Composable
fun ShortcutList(
    hasMultipleCategories: Boolean,
    shortcutListItems: List<ShortcutListItemModel>,
    layoutType: CategoryLayoutType,
    textColor: Color?,
    useTextShadows: Boolean,
    scale: Float,
    isLongClickingEnabled: Boolean,
    onShortcutClicked: (ShortcutId) -> Unit,
    onShortcutLongClicked: (ShortcutId) -> Unit,
) {
    if (shortcutListItems.isEmpty()) {
        if (hasMultipleCategories) {
            EmptyState(
                description = stringResource(R.string.empty_state_no_shortcuts_in_category),
            )
        } else {
            EmptyState(
                title = stringResource(R.string.empty_state_shortcuts),
                description = stringResource(R.string.empty_state_shortcuts_instructions),
            )
        }
    }

    val textStyle = TextStyle.Default
        .copy(fontSize = FontSize.SMALL * ((scale - 1f) / 2 + 1f))
        .runIf(useTextShadows) { copy(shadow = DefaultTextShadow) }

    if (layoutType == CategoryLayoutType.LINEAR_LIST) {
        ShortcutLinearList(
            shortcutListItems = shortcutListItems,
            textColor = textColor,
            textStyle = textStyle,
            scale = scale,
            isLongClickingEnabled = isLongClickingEnabled,
            onShortcutClicked = onShortcutClicked,
            onShortcutLongClicked = onShortcutLongClicked,
        )
    } else {
        ShortcutGrid(
            shortcutListItems = shortcutListItems,
            minColumnWidth = when (layoutType) {
                CategoryLayoutType.DENSE_GRID -> 78.dp
                CategoryLayoutType.MEDIUM_GRID -> 120.dp
                CategoryLayoutType.WIDE_GRID -> 180.dp
                else -> error("This can not be reached, but the compiler is not smart enough to understand that")
            } * ((scale - 1f) / 2 + 1f),
            textColor = textColor,
            textStyle = textStyle,
            scale = scale,
            isLongClickingEnabled = isLongClickingEnabled,
            onShortcutClicked = onShortcutClicked,
            onShortcutLongClicked = onShortcutLongClicked,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ShortcutLinearList(
    shortcutListItems: List<ShortcutListItemModel>,
    textColor: Color?,
    textStyle: TextStyle,
    scale: Float,
    isLongClickingEnabled: Boolean,
    onShortcutClicked: (ShortcutId) -> Unit,
    onShortcutLongClicked: (ShortcutId) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
    ) {
        shortcutListItems.forEachIndexed { index, item ->
            when (item) {
                is ShortcutListItemModel.Section -> item(
                    key = "section_${item.id}",
                    contentType = "section",
                ) {
                    Column {
                        Section(
                            modifier = Modifier.padding(
                                top = Spacing.MEDIUM * if (index == 0) 1 else 2,
                                bottom = Spacing.SMALL,
                            ),
                            textColor = textColor,
                            scale = scale,
                            section = item,
                        )
                        HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.3f))
                    }
                }
                is ShortcutListItemModel.EmptyState -> item(
                    key = "empty_${item.id}",
                    contentType = "empty-state",
                ) {
                    EmptySection(
                        textColor = textColor,
                    )
                }
                is ShortcutListItemModel.ShortcutItem -> item(
                    key = item.id,
                    contentType = "shortcut",
                ) {
                    ShortcutListItem(
                        shortcut = item,
                        textColor = textColor,
                        textStyle = textStyle,
                        scale = scale,
                        modifier = Modifier
                            .animateItem()
                            .combinedClickable(
                                onLongClick = if (isLongClickingEnabled) {
                                    {
                                        onShortcutLongClicked(item.id)
                                    }
                                } else {
                                    null
                                },
                                onClick = {
                                    onShortcutClicked(item.id)
                                },
                            ),
                    )
                }
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
private fun Section(
    modifier: Modifier,
    section: ShortcutListItemModel.Section,
    textColor: Color?,
    scale: Float,
) {
    val fontSize = FontSize.BIG * ((scale - 1f) / 2 + 1f)
    Text(
        modifier = modifier
            .semantics {
                heading()
            }
            .padding(horizontal = Spacing.MEDIUM)
            .fillMaxWidth(),
        text = section.name,
        color = textColor ?: Color.Unspecified,
        fontSize = fontSize,
        lineHeight = fontSize * 0.8f,
        fontWeight = FontWeight.Bold,
        maxLines = 3,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun EmptySection(
    textColor: Color?,
) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(0.8f),
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent,
        ),
        headlineContent = {
            Text(
                text = stringResource(R.string.placeholder_empty_category_section_main_screen, stringResource(R.string.action_move)),
                fontStyle = FontStyle.Italic,
                fontSize = FontSize.SMALL,
                color = textColor ?: Color.Unspecified,
            )
        },
    )
}

@Composable
private fun ShortcutListItem(
    shortcut: ShortcutListItemModel.ShortcutItem,
    modifier: Modifier = Modifier,
    textColor: Color?,
    textStyle: TextStyle,
    scale: Float,
) {
    Column(
        modifier,
    ) {
        ListItem(
            modifier = Modifier
                .fillMaxWidth()
                .runIf(shortcut.isHidden) {
                    alpha(HIDDEN_ALPHA)
                },
            colors = ListItemDefaults.colors(
                containerColor = Color.Transparent,
            ),
            headlineContent = {
                Text(
                    text = shortcut.name,
                    color = textColor ?: Color.Unspecified,
                    style = textStyle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            supportingContent = shortcut.description.takeUnlessEmpty()?.let {
                {
                    Text(
                        text = shortcut.description,
                        color = textColor ?: Color.Unspecified,
                        style = textStyle,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            },
            leadingContent = {
                ShortcutIcon(
                    shortcut.icon,
                    size = 44.dp * scale,
                )
            },
            trailingContent = if (shortcut.isPending) {
                {
                    // TODO: Add background / shadow if needed
                    // TODO: Animate in & out
                    Icon(Icons.Outlined.HourglassEmpty, null)
                }
            } else {
                null
            },
        )
        HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.3f))
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ShortcutGrid(
    shortcutListItems: List<ShortcutListItemModel>,
    minColumnWidth: Dp,
    textColor: Color?,
    textStyle: TextStyle,
    scale: Float,
    isLongClickingEnabled: Boolean,
    onShortcutClicked: (ShortcutId) -> Unit,
    onShortcutLongClicked: (ShortcutId) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = minColumnWidth),
        modifier = Modifier
            .fillMaxSize()
            .padding(
                horizontal = 2.dp,
            ),
    ) {
        shortcutListItems.forEach { item ->
            when (item) {
                is ShortcutListItemModel.Section -> item(
                    key = "section_${item.id}",
                    contentType = "section",
                    span = {
                        GridItemSpan(maxLineSpan)
                    },
                ) {
                    Section(
                        modifier = Modifier.padding(top = Spacing.MEDIUM, bottom = Spacing.SMALL),
                        section = item,
                        textColor = textColor,
                        scale = scale,
                    )
                }
                is ShortcutListItemModel.EmptyState -> item(
                    key = "empty_${item.id}",
                    contentType = "empty-state",
                    span = {
                        GridItemSpan(maxLineSpan)
                    },
                ) {
                    EmptySection(
                        textColor = textColor,
                    )
                }
                is ShortcutListItemModel.ShortcutItem -> item(
                    key = item.id,
                    contentType = "shortcut",
                ) {
                    ShortcutGridItem(
                        shortcut = item,
                        textColor = textColor,
                        textStyle = textStyle,
                        scale = scale,
                        modifier = Modifier
                            .animateItem()
                            .combinedClickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = false, radius = 48.dp),
                                onLongClick = if (isLongClickingEnabled) {
                                    {
                                        onShortcutLongClicked(item.id)
                                    }
                                } else {
                                    null
                                },
                                onClick = {
                                    onShortcutClicked(item.id)
                                },
                            )
                            .padding(Spacing.SMALL),
                    )
                }
            }
        }

        item(
            key = "spacer",
            contentType = "spacer",
            span = {
                GridItemSpan(maxLineSpan)
            },
        ) {
            VerticalSpacer(Spacing.HUGE)
        }
    }
}

@Composable
private fun ShortcutGridItem(
    shortcut: ShortcutListItemModel.ShortcutItem,
    modifier: Modifier,
    textColor: Color?,
    textStyle: TextStyle,
    scale: Float,
) {
    Column(
        modifier = modifier
            .runIf(shortcut.isHidden) {
                alpha(HIDDEN_ALPHA)
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.SMALL, Alignment.CenterVertically),
    ) {
        Box {
            ShortcutIcon(
                shortcut.icon,
                size = 44.dp * scale,
            )
            if (shortcut.isPending) {
                // TODO: Add background / shadow if needed
                // TODO: Animate in & out
                Icon(
                    Icons.Outlined.HourglassEmpty,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier
                        .size(16.dp)
                        .background(
                            color = MaterialTheme.colorScheme.secondary,
                            shape = CircleShape,
                        )
                        .padding(2.dp)
                        .align(Alignment.BottomEnd),
                )
            }
        }

        Text(
            shortcut.name,
            color = textColor ?: Color.Unspecified,
            style = textStyle,
            maxLines = 2,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

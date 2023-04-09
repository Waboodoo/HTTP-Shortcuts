package ch.rmy.android.http_shortcuts.activities.icons

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import ch.rmy.android.framework.extensions.runIf
import ch.rmy.android.http_shortcuts.components.ShortcutIcon
import ch.rmy.android.http_shortcuts.components.Spacing
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon as ShortcutIconModel

@Composable
fun IconPickerContent(
    viewState: IconPickerViewState,
    onIconClicked: (ShortcutIconModel.CustomIcon) -> Unit,
    onIconLongPressed: (ShortcutIconModel.CustomIcon) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 44.dp),
        contentPadding = PaddingValues(Spacing.MEDIUM),
        verticalArrangement = Arrangement.spacedBy(Spacing.MEDIUM),
        horizontalArrangement = Arrangement.spacedBy(Spacing.MEDIUM),
    ) {
        items(
            items = viewState.icons,
            key = {
                it.icon.fileName
            },
        ) { iconItem ->
            IconItem(
                icon = iconItem.icon,
                isUnused = iconItem.isUnused,
                onIconClicked = {
                    onIconClicked(iconItem.icon)
                },
                onIconLongPressed = {
                    onIconLongPressed(iconItem.icon)
                },
            )
        }
    }

    // TODO: Show an empty state when there are no icons
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun IconItem(
    icon: ShortcutIconModel.CustomIcon,
    isUnused: Boolean,
    onIconClicked: () -> Unit,
    onIconLongPressed: () -> Unit,
) {
    ShortcutIcon(
        icon,
        modifier = Modifier.fillMaxWidth()
            .combinedClickable(
                onLongClick = onIconLongPressed,
                onClick = onIconClicked,
            )
            .runIf(isUnused) {
                alpha(0.6f)
            },
    )
}

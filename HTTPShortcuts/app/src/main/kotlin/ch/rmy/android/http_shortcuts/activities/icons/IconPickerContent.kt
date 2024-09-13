package ch.rmy.android.http_shortcuts.activities.icons

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.rmy.android.framework.extensions.runIf
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.EmptyState
import ch.rmy.android.http_shortcuts.components.ShortcutIcon
import ch.rmy.android.http_shortcuts.components.Spacing
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon as ShortcutIconModel

@Composable
fun IconPickerContent(
    viewState: IconPickerViewState,
    onIconClicked: (ShortcutIconModel.CustomIcon) -> Unit,
    onIconLongPressed: (ShortcutIconModel.CustomIcon) -> Unit,
) {
    if (viewState.icons.isEmpty()) {
        EmptyState(
            description = stringResource(R.string.empty_state_custom_icons),
        )
        return
    }

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
        contentDescription = stringResource(R.string.icon_description),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false),
                onLongClick = onIconLongPressed,
                onClick = onIconClicked,
            )
            .runIf(isUnused) {
                alpha(0.6f)
            },
    )
}

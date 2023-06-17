package ch.rmy.android.http_shortcuts.components

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.icons.Icons
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon

private const val STATE_BUILT_IN = "built-in"
private const val STATE_COLOR_PICKER = "color-picker"

@Composable
fun IconPickerDialog(
    title: String,
    onCustomIconOptionSelected: () -> Unit,
    onIconSelected: (ShortcutIcon) -> Unit,
    onFaviconOptionSelected: (() -> Unit)? = null,
    onDismissRequested: () -> Unit,
) {
    var state by rememberSaveable(key = "icon-picker-state") {
        mutableStateOf("")
    }
    var persistedIcon by rememberSaveable(key = "icon-picker-icon") {
        mutableStateOf("")
    }
    val icon = remember(persistedIcon) {
        ShortcutIcon.fromName(persistedIcon) as ShortcutIcon.BuiltInIcon
    }

    when (state) {
        STATE_BUILT_IN -> {
            BuiltInIconPicker(
                onIconSelected = {
                    if (it.tint != null) {
                        persistedIcon = it.iconName
                        state = STATE_COLOR_PICKER
                    } else {
                        onIconSelected(it)
                    }
                },
                onDismissRequested = onDismissRequested,
            )
        }

        STATE_COLOR_PICKER -> {
            ColorPickerDialog(
                extraContent = { color ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        ShortcutIcon(
                            icon.withTint(color),
                        )
                    }
                },
                onColorSelected = { color ->
                    onIconSelected((ShortcutIcon.fromName(persistedIcon) as ShortcutIcon.BuiltInIcon).withTint(color))
                },
                onDismissRequested = onDismissRequested,
            )
        }

        else -> {
            OptionsDialog(
                title,
                onBuiltInIconOptionSelected = {
                    state = STATE_BUILT_IN
                },
                onCustomIconOptionSelected = onCustomIconOptionSelected,
                onFaviconOptionSelected = onFaviconOptionSelected,
                onDismissRequested = onDismissRequested,
            )
        }
    }
}

@Composable
private fun OptionsDialog(
    title: String,
    onBuiltInIconOptionSelected: () -> Unit,
    onCustomIconOptionSelected: () -> Unit,
    onFaviconOptionSelected: (() -> Unit)? = null,
    onDismissRequested: () -> Unit,
) {
    SelectDialog(
        title = title,
        onDismissRequest = onDismissRequested,
    ) {
        SelectDialogEntry(
            label = stringResource(R.string.choose_icon),
            onClick = onBuiltInIconOptionSelected,
        )
        SelectDialogEntry(
            label = stringResource(R.string.choose_image),
            onClick = onCustomIconOptionSelected,
        )
        if (onFaviconOptionSelected != null) {
            SelectDialogEntry(
                label = stringResource(R.string.choose_page_favicon),
                onClick = {
                    onFaviconOptionSelected()
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BuiltInIconPicker(
    onIconSelected: (ShortcutIcon.BuiltInIcon) -> Unit,
    onDismissRequested: () -> Unit,
) {
    val context = LocalContext.current
    val coloredIcons = remember {
        getColoredIcons(context)
    }
    val isDarkMode = isSystemInDarkTheme()
    val tintableIcons = remember(isDarkMode) {
        getTintableIcons(context, if (isDarkMode) Icons.TintColor.WHITE else Icons.TintColor.BLACK)
    }

    AlertDialog(
        modifier = Modifier
            .padding(vertical = Spacing.MEDIUM),
        onDismissRequest = onDismissRequested,
    ) {
        Surface(
            shape = AlertDialogDefaults.shape,
            tonalElevation = AlertDialogDefaults.TonalElevation,
            color = AlertDialogDefaults.containerColor,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    stringResource(R.string.title_choose_icon),
                    fontSize = FontSize.HUGE,
                    modifier = Modifier.padding(Spacing.MEDIUM),
                )

                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 44.dp),
                    contentPadding = PaddingValues(Spacing.MEDIUM),
                    verticalArrangement = Arrangement.spacedBy(Spacing.MEDIUM),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.MEDIUM),
                ) {
                    iconSection(coloredIcons, onIconSelected)
                    item(key = "divider", contentType = "divider", span = { GridItemSpan(maxLineSpan) }) {
                        Divider(
                            modifier = Modifier.padding(vertical = Spacing.SMALL),
                        )
                    }
                    iconSection(tintableIcons, onIconSelected)
                }
            }
        }
    }
}

private fun LazyGridScope.iconSection(
    icons: List<ShortcutIcon.BuiltInIcon>,
    onIconSelected: (ShortcutIcon.BuiltInIcon) -> Unit,
) {
    items(
        items = icons,
        key = {
            it.iconName
        },
        contentType = {
            "icon"
        },
    ) { icon ->
        IconItem(
            icon = icon,
            onIconClicked = {
                onIconSelected(icon)
            },
        )
    }
}

@Composable
private fun IconItem(
    icon: ShortcutIcon,
    onIconClicked: () -> Unit,
) {
    ShortcutIcon(
        icon,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(bounded = false),
                onClick = onIconClicked,
            ),
    )
}

private fun getColoredIcons(context: Context): List<ShortcutIcon.BuiltInIcon> =
    Icons.getColoredIcons()
        .map {
            ShortcutIcon.BuiltInIcon.fromDrawableResource(context, it)
        }

private fun getTintableIcons(context: Context, tintColor: Icons.TintColor): List<ShortcutIcon.BuiltInIcon> =
    Icons.getTintableIcons().map { iconResource ->
        ShortcutIcon.BuiltInIcon.fromDrawableResource(context, iconResource, tintColor)
    }

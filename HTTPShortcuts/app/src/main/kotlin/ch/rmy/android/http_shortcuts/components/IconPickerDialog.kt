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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.icons.IconFilterProvider
import ch.rmy.android.http_shortcuts.icons.Icons
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

private const val STATE_BUILT_IN = "built-in"
private const val STATE_COLOR_PICKER = "color-picker"

@Composable
fun IconPickerDialog(
    title: String,
    currentIcon: ShortcutIcon.BuiltInIcon? = null,
    suggestionBase: String? = null,
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
        if (persistedIcon.isNotEmpty()) {
            ShortcutIcon.fromName(persistedIcon) as? ShortcutIcon.BuiltInIcon
        } else {
            null
        }
    }

    when (state) {
        STATE_BUILT_IN -> {
            BuiltInIconPicker(
                activeIcon = currentIcon,
                suggestionBase = suggestionBase,
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
                            icon?.withTint(color) ?: ShortcutIcon.NoIcon,
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
            // Init iconFilterProvider here such that it can already start building up the global in-memory search index
            val context = LocalContext.current
            val iconFilterProvider = remember {
                IconFilterProvider(context)
            }
            LaunchedEffect(iconFilterProvider) {
                iconFilterProvider.init()
            }

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
    activeIcon: ShortcutIcon.BuiltInIcon? = null,
    suggestionBase: String? = null,
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
    val allIcons = remember {
        coloredIcons + tintableIcons
    }

    val iconFilterProvider = remember {
        IconFilterProvider(context)
    }
    var suggestions by remember {
        mutableStateOf(emptyList<ShortcutIcon.BuiltInIcon>())
    }
    LaunchedEffect(iconFilterProvider, activeIcon, suggestionBase) {
        iconFilterProvider.init()
        if (suggestionBase != null) {
            suggestions = withContext(Dispatchers.Default) {
                iconFilterProvider.createScoringFunction(suggestionBase)
                    ?.let { scoringFunction ->
                        allIcons
                            .asSequence()
                            .map { it to scoringFunction(it) }
                            .filter { (_, score) -> score != 0 }
                            .sortedByDescending { (_, score) -> score }
                            .map { (icon, _) -> icon }
                            .filterNot { it.normalizedIconName == activeIcon?.normalizedIconName }
                            .take(4)
                            .toList()
                    }
                    ?.toList()
                    ?: emptyList()
            }
        }
    }
    val topRowIcons = remember(activeIcon, suggestions) {
        listOfNotNull(activeIcon) + suggestions
    }
    var searchQuery by remember {
        mutableStateOf("")
    }
    var filteredIcons by remember {
        mutableStateOf<List<ShortcutIcon.BuiltInIcon>?>(null)
    }
    LaunchedEffect(searchQuery, allIcons) {
        if (searchQuery.isBlank()) {
            filteredIcons = null
        } else {
            delay(300.milliseconds)
            filteredIcons = withContext(Dispatchers.Default) {
                iconFilterProvider.createScoringFunction(searchQuery)
                    ?.let { scoringFunction ->
                        allIcons
                            .map { it to scoringFunction(it) }
                            .filter { (_, score) -> score != 0 }
                            .sortedByDescending { (_, score) -> score }
                            .map { (icon, _) -> icon }
                    }
            }
        }
    }

    Dialog(
        onDismissRequest = onDismissRequested,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier.padding(Spacing.MEDIUM),
            shape = AlertDialogDefaults.shape,
            tonalElevation = AlertDialogDefaults.TonalElevation,
            color = AlertDialogDefaults.containerColor,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    stringResource(R.string.title_choose_icon),
                    fontSize = FontSize.HUGE,
                    modifier = Modifier.padding(Spacing.MEDIUM),
                )

                SearchBar(
                    query = searchQuery,
                    onQueryChanged = {
                        searchQuery = it
                    },
                )

                LazyVerticalGrid(
                    state = rememberSaveable(filteredIcons, saver = LazyGridState.Saver) {
                        LazyGridState()
                    },
                    columns = GridCells.Adaptive(minSize = 44.dp),
                    contentPadding = PaddingValues(Spacing.MEDIUM),
                    verticalArrangement = Arrangement.spacedBy(Spacing.MEDIUM),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.MEDIUM),
                ) {
                    filteredIcons?.let {
                        if (it.isEmpty()) {
                            noResults()
                        } else {
                            iconSection(it, onIconSelected)
                        }
                    }
                        ?: run {
                            if (topRowIcons.isNotEmpty()) {
                                iconSection(topRowIcons, onIconSelected, keySuffix = "-top")
                                divider()
                            }
                            iconSection(coloredIcons, onIconSelected)
                            divider()
                            iconSection(tintableIcons, onIconSelected)
                        }
                }
            }
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChanged: (String) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.SMALL),
    ) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = query,
            onValueChange = onQueryChanged,
            placeholder = {
                Text(stringResource(R.string.menu_action_search))
            },
            leadingIcon = {
                Icon(androidx.compose.material.icons.Icons.Outlined.Search, contentDescription = null)
            },
            maxLines = 1,
            singleLine = true,
        )
    }
}

private fun LazyGridScope.divider() {
    item(key = "divider", contentType = "divider", span = { GridItemSpan(maxLineSpan) }) {
        HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.SMALL))
    }
}

private fun LazyGridScope.noResults() {
    item(key = "noResults", contentType = "noResults", span = { GridItemSpan(maxLineSpan) }) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.SMALL),
            text = stringResource(R.string.instructions_search_no_results),
            textAlign = TextAlign.Center,
        )
    }
}

private fun LazyGridScope.iconSection(
    icons: List<ShortcutIcon.BuiltInIcon>,
    onIconSelected: (ShortcutIcon.BuiltInIcon) -> Unit,
    keySuffix: String = "",
) {
    items(
        items = icons,
        key = {
            it.iconName + keySuffix
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
    icon: ShortcutIcon.BuiltInIcon,
    onIconClicked: () -> Unit,
) {
    Box(
        modifier = Modifier.size(44.dp),
    ) {
        ShortcutIcon(
            icon,
            contentDescription = icon.plainName,
            modifier = Modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = false),
                    onClick = onIconClicked,
                ),
        )
    }
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

@Preview
@Composable
private fun BuiltInIconPicker_Preview() {
    BuiltInIconPicker(
        onIconSelected = {},
        onDismissRequested = {},
    )
}

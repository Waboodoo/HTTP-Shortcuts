package ch.rmy.android.http_shortcuts.activities.icons

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.extensions.runIf
import ch.rmy.android.framework.extensions.runIfNotNull
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.icons.models.MaterialIcon
import ch.rmy.android.http_shortcuts.components.ColorPickerDialog
import ch.rmy.android.http_shortcuts.components.FontSize
import ch.rmy.android.http_shortcuts.components.LoadingIndicator
import ch.rmy.android.http_shortcuts.components.SearchBar
import ch.rmy.android.http_shortcuts.components.Spacing
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.request.error
import coil3.request.fallback
import coil3.request.placeholder
import kotlinx.coroutines.CancellationException
import java.io.IOException
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay

@Composable
fun MaterialIconPickerDialog(
    getIcons: suspend (String) -> List<MaterialIcon>,
    onIconSelected: (MaterialIcon, color: Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var selectedIcon by remember {
        mutableStateOf<MaterialIcon?>(null)
    }
    var searchQuery by remember {
        mutableStateOf("")
    }
    var iconsState by remember {
        mutableStateOf<IconsState>(IconsState.Loading)
    }
    val gridState = rememberSaveable(iconsState, saver = LazyGridState.Saver) {
        LazyGridState()
    }

    LaunchedEffect(searchQuery) {
        try {
            if (!searchQuery.isBlank()) {
                delay(300.milliseconds)
            }
            iconsState = IconsState.Success(getIcons(searchQuery))
        } catch (_: IOException) {
            iconsState = IconsState.Failed
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logException("MaterialIconPickerDialog", e)
            iconsState = IconsState.Failed
        }
    }

    if (selectedIcon != null) {
        val context = LocalContext.current
        val imageLoader = remember {
            createImageLoader(context)
        }
        ColorPickerDialog(
            extraContent = { color ->
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    IconItem(
                        icon = selectedIcon!!,
                        colorFilter = ColorFilter.tint(Color(color)),
                        imageLoader = imageLoader,
                    )
                }
            },
            onColorSelected = { color ->
                onIconSelected(selectedIcon!!, color)
            },
            onDismissRequested = {
                selectedIcon = null
            },
        )
    } else {
        IconPickerDialog(
            gridState = gridState,
            iconsState = iconsState,
            searchQuery = searchQuery,
            onIconSelected = {
                selectedIcon = it
            },
            onSearchQueryChanged = {
                searchQuery = it
            },
            onDismiss = onDismiss,
        )
    }
}

@Composable
private fun IconPickerDialog(
    gridState: LazyGridState,
    iconsState: IconsState,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    onIconSelected: (MaterialIcon) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val imageLoader = remember {
        createImageLoader(context)
    }
    val isDarkMode = isSystemInDarkTheme()
    val colorFilter = remember(isDarkMode) {
        ColorFilter.tint(if (isDarkMode) Color.White else Color.Black)
    }

    Dialog(
        onDismissRequest = onDismiss,
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
                    stringResource(R.string.dialog_title_material_design_icons),
                    fontSize = FontSize.HUGE,
                    modifier = Modifier.padding(Spacing.MEDIUM),
                )

                SearchBar(
                    query = searchQuery,
                    onQueryChanged = onSearchQueryChanged,
                )

                LazyVerticalGrid(
                    state = gridState,
                    columns = GridCells.Adaptive(minSize = 44.dp),
                    contentPadding = PaddingValues(Spacing.MEDIUM),
                    verticalArrangement = Arrangement.spacedBy(Spacing.MEDIUM),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.MEDIUM),
                ) {
                    when (val iconsState = iconsState) {
                        IconsState.Failed -> item(key = "_error", contentType = "error", span = { GridItemSpan(maxLineSpan) }) {
                            TextItem(stringResource(R.string.instructions_icon_search_failed))
                        }
                        IconsState.Loading -> item(span = { GridItemSpan(maxLineSpan) }) {
                            LoadingIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(80.dp)
                                    .padding(Spacing.MEDIUM),
                            )
                        }
                        is IconsState.Success -> {
                            val icons = iconsState.icons
                            if (icons.isEmpty()) {
                                item(key = "_empty", contentType = "empty", span = { GridItemSpan(maxLineSpan) }) {
                                    TextItem(stringResource(R.string.instructions_search_no_results))
                                }
                            } else {
                                items(
                                    items = icons,
                                    key = {
                                        it.name
                                    },
                                    contentType = {
                                        "icon"
                                    },
                                ) { icon ->
                                    IconItem(
                                        icon = icon,
                                        imageLoader = imageLoader,
                                        colorFilter = colorFilter,
                                        onClick = {
                                            onIconSelected(icon)
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TextItem(text: String) {
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.SMALL),
        text = text,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun IconItem(
    icon: MaterialIcon,
    colorFilter: ColorFilter,
    imageLoader: ImageLoader,
    onClick: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    var loaded by remember { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier.size(44.dp),
    ) {
        val model = remember(icon) {
            ImageRequest.Builder(context)
                .data(icon.url)
                .fallback(R.drawable.image_placeholder)
                .placeholder(R.drawable.image_placeholder)
                .error(R.drawable.bitsies_cancel)
                .crossfade(true)
                .build()
        }

        AsyncImage(
            model = model,
            contentDescription = icon.name,
            imageLoader = imageLoader,
            colorFilter = colorFilter,
            onSuccess = {
                loaded = true
                failed = false
            },
            onError = {
                failed = true
            },
            modifier = Modifier
                .width(44.dp)
                .aspectRatio(1f)
                .runIf(failed) {
                    alpha(0.2f)
                }
                .runIfNotNull(onClick) { onClick ->
                    clickable(
                        enabled = loaded,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = false),
                        onClick = onClick,
                    )
                },
        )
    }
}

@Stable
private sealed class IconsState {
    @Stable
    data object Loading : IconsState()

    @Stable
    data object Failed : IconsState()

    @Stable
    data class Success(val icons: List<MaterialIcon>) : IconsState()
}

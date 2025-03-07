package ch.rmy.android.http_shortcuts.activities.documentation

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.rmy.android.framework.extensions.consume
import ch.rmy.android.framework.extensions.openURL
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.documentation.models.SearchDirection
import ch.rmy.android.http_shortcuts.components.EventHandler
import ch.rmy.android.http_shortcuts.components.FontSize
import ch.rmy.android.http_shortcuts.components.SimpleScaffold
import ch.rmy.android.http_shortcuts.components.Spacing
import ch.rmy.android.http_shortcuts.components.ToolbarIcon
import ch.rmy.android.http_shortcuts.components.bindViewModel
import ch.rmy.android.http_shortcuts.extensions.runIf
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

@Composable
fun DocumentationScreen(url: Uri?) {
    val (viewModel, state) = bindViewModel<DocumentationViewModel.InitData, DocumentationViewState, DocumentationViewModel>(
        DocumentationViewModel.InitData(url),
    )

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    EventHandler { event ->
        when (event) {
            is DocumentationEvent.OpenInBrowser -> consume {
                context.openURL(event.url)
            }
            else -> false
        }
    }

    var subtitle by remember {
        mutableStateOf<String?>(null)
    }
    var searchQuery by remember {
        mutableStateOf<String?>(null)
    }
    var searchResults by remember {
        mutableStateOf<Pair<Int, Int>?>(null)
    }
    val searchDirectionRequests = remember {
        MutableSharedFlow<SearchDirection>()
    }

    SimpleScaffold(
        viewState = state,
        title = stringResource(R.string.title_documentation),
        subtitle = subtitle,
        actions = {
            ToolbarIcon(
                Icons.Filled.Search,
                contentDescription = stringResource(R.string.menu_action_search),
                onClick = {
                    searchQuery = if (searchQuery == null) "" else null
                },
            )
            ToolbarIcon(
                Icons.Filled.OpenInBrowser,
                contentDescription = stringResource(R.string.button_open_documentation_in_browser),
                onClick = viewModel::onOpenInBrowserButtonClicked,
            )
        },
    ) { viewState ->
        Box {
            DocumentationContent(
                url = viewState.url,
                searchQuery = searchQuery,
                searchDirectionRequests = searchDirectionRequests,
                onPageChanged = {
                    searchQuery = null
                    viewModel.onPageChanged(it)
                },
                onPageTitle = { subtitle = it },
                onExternalUrl = viewModel::onExternalUrl,
                onSearchResults = { current, total ->
                    searchResults = Pair(current, total)
                },
            )

            searchQuery?.let {
                BackHandler {
                    searchQuery = null
                }

                SearchBar(
                    query = it,
                    results = searchResults,
                    onQueryChanged = { newQuery ->
                        searchQuery = newQuery
                    },
                    onNext = { direction ->
                        coroutineScope.launch {
                            searchDirectionRequests.emit(direction)
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun SearchBar(
    modifier: Modifier = Modifier,
    query: String,
    results: Pair<Int, Int>?,
    onQueryChanged: (String?) -> Unit,
    onNext: (SearchDirection) -> Unit,
) {
    val focusRequester = remember {
        FocusRequester()
    }
    val keyboard = LocalSoftwareKeyboardController.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(Spacing.TINY)
            .shadow(elevation = 1.dp)
            .then(modifier),
        horizontalArrangement = Arrangement.spacedBy(Spacing.TINY),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextField(
            value = query,
            onValueChange = onQueryChanged,
            singleLine = true,
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester),
            placeholder = {
                Text(text = stringResource(R.string.placeholder_documentation_search_query))
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
            keyboardActions = KeyboardActions {
                onQueryChanged(null)
            },
        )

        results?.takeIf { query.isNotEmpty() }?.let { (current, total) ->
            Text(
                text = "$current/$total",
                maxLines = 1,
                fontSize = FontSize.SMALL,
            )
        }

        Row(modifier = Modifier.padding(end = Spacing.SMALL)) {
            val enabled = results?.second?.let { it > 1 } == true
            Icon(
                Icons.Outlined.KeyboardArrowUp,
                contentDescription = stringResource(R.string.accessibility_search_go_to_previous),
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = false),
                        enabled = enabled,
                        onClick = {
                            onNext(SearchDirection.PREVIOUS)
                        },
                    )
                    .padding(horizontal = Spacing.SMALL)
                    .runIf(!enabled) {
                        alpha(0.3f)
                    },
            )
            Icon(
                Icons.Outlined.KeyboardArrowDown,
                contentDescription = stringResource(R.string.accessibility_search_go_to_next),
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = false),
                        enabled = enabled,
                        onClick = {
                            onNext(SearchDirection.NEXT)
                        },
                    )
                    .padding(horizontal = Spacing.SMALL)
                    .runIf(!enabled) {
                        alpha(0.3f)
                    },
            )
        }

        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
            delay(50.milliseconds)
            keyboard?.show()
        }
    }
}

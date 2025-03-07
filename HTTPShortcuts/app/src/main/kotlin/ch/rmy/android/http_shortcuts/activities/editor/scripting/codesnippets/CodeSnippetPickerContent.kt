package ch.rmy.android.http_shortcuts.activities.editor.scripting.codesnippets

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.collapse
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.expand
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import ch.rmy.android.framework.extensions.consume
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.editor.scripting.codesnippets.models.ItemWrapper
import ch.rmy.android.http_shortcuts.components.EmptyState
import ch.rmy.android.http_shortcuts.components.Spacing
import ch.rmy.android.http_shortcuts.extensions.localize

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CodeSnippetPickerContent(
    query: String,
    items: List<ItemWrapper>,
    onQueryChanged: (String) -> Unit,
    onSectionClicked: (String) -> Unit,
    onCodeSnippetItemClicked: (String) -> Unit,
    onDocumentationButtonClicked: (String) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        SearchBar(query, onQueryChanged)

        if (items.isEmpty()) {
            EmptyState(
                description = stringResource(R.string.instructions_search_no_results),
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
            ) {
                items(
                    items = items,
                    key = { it.id },
                    contentType = {
                        when (it) {
                            is ItemWrapper.CodeSnippet -> "code-snippet"
                            is ItemWrapper.Section -> "section"
                        }
                    },
                ) { itemWrapper ->
                    when (itemWrapper) {
                        is ItemWrapper.CodeSnippet -> {
                            with(itemWrapper.codeSnippetItem) {
                                CodeSnippetItem(
                                    modifier = Modifier.animateItem(),
                                    title = title.localize(),
                                    description = description?.localize(),
                                    onDocumentationButtonClicked = docRef?.let {
                                        {
                                            onDocumentationButtonClicked(itemWrapper.id)
                                        }
                                    },
                                    onClicked = {
                                        onCodeSnippetItemClicked(itemWrapper.id)
                                    },
                                )
                            }
                        }
                        is ItemWrapper.Section -> {
                            Section(
                                modifier = Modifier.animateItem(),
                                title = itemWrapper.sectionItem.title.localize(),
                                icon = itemWrapper.sectionItem.icon,
                                expanded = itemWrapper.expanded,
                                onClicked = {
                                    onSectionClicked(itemWrapper.id)
                                },
                            )
                        }
                    }
                }

                item(key = "divider", contentType = "divider") {
                    HorizontalDivider()
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
            .background(MaterialTheme.colorScheme.surface)
            .padding(Spacing.SMALL),
    ) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = query,
            onValueChange = onQueryChanged,
            placeholder = {
                Text(stringResource(R.string.menu_action_search))
            },
            leadingIcon = {
                Icon(Icons.Outlined.Search, contentDescription = null)
            },
            maxLines = 1,
            singleLine = true,
        )
    }
}

@Composable
private fun Section(
    modifier: Modifier,
    title: String,
    icon: Int,
    expanded: Boolean,
    onClicked: () -> Unit,
) {
    val rotationDegrees by animateFloatAsState(targetValue = if (expanded) 90f else 0f)
    Column(modifier) {
        HorizontalDivider()
        ListItem(
            modifier = Modifier
                .semantics {
                    if (expanded) {
                        collapse {
                            consume(onClicked)
                        }
                    } else {
                        expand {
                            consume(onClicked)
                        }
                    }
                }
                .clickable(onClick = onClicked),
            leadingContent = {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
            },
            headlineContent = {
                Text(title)
            },
            trailingContent = {
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier
                        .rotate(rotationDegrees),
                )
            },
        )
    }
}

@Composable
private fun CodeSnippetItem(
    modifier: Modifier,
    title: String,
    description: String?,
    onDocumentationButtonClicked: (() -> Unit)?,
    onClicked: () -> Unit,
) {
    val resources = LocalContext.current.resources
    ListItem(
        modifier = Modifier
            .semantics(mergeDescendants = true) {
                onDocumentationButtonClicked?.let {
                    customActions = listOf(
                        CustomAccessibilityAction(
                            label = resources.getString(R.string.accessibility_label_show_documentation),
                            action = {
                                consume { onDocumentationButtonClicked() }
                            },
                        ),
                    )
                }
            }
            .clickable(onClick = onClicked)
            .then(modifier),
        leadingContent = {
            Spacer(modifier = Modifier.width(20.dp))
        },
        headlineContent = {
            Text(title)
        },
        supportingContent = description?.let {
            {
                Text(description)
            }
        },
        trailingContent = onDocumentationButtonClicked?.let {
            {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                    contentDescription = null,
                    modifier = Modifier
                        .clearAndSetSemantics { }
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(bounded = false),
                            onClick = onDocumentationButtonClicked,
                        )
                        .alpha(0.8f)
                        .size(28.dp)
                        .padding(4.dp),
                )
            }
        },
    )
}

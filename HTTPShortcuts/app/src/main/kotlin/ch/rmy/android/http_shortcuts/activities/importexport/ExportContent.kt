package ch.rmy.android.http_shortcuts.activities.importexport

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.importexport.models.ExportItem
import ch.rmy.android.http_shortcuts.components.FontSize
import ch.rmy.android.http_shortcuts.components.ScreenInstructionsHeaders
import ch.rmy.android.http_shortcuts.components.ShortcutIcon
import ch.rmy.android.http_shortcuts.components.Spacing
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.extensions.runIf
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon

@Composable
fun ExportContent(
    items: List<ExportItem>,
    onShortcutCheckedChanged: (ShortcutId, Boolean) -> Unit,
    onCategoryCheckedChanged: (CategoryId, Boolean) -> Unit,
) {
    Column {
        ScreenInstructionsHeaders(stringResource(R.string.instructions_export))
        LazyColumn(
            state = rememberLazyListState(),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(
                    vertical = Spacing.MEDIUM,
                ),
            verticalArrangement = Arrangement.spacedBy(Spacing.SMALL),
        ) {
            items.forEachIndexed { index, item ->
                when (item) {
                    is ExportItem.Category -> {
                        item(
                            key = "category_${item.categoryId}",
                            contentType = "category",
                        ) {
                            CategoryHeader(
                                modifier = Modifier.runIf(index != 0) {
                                    padding(top = Spacing.MEDIUM)
                                },
                                name = item.name,
                                checked = item.checked,
                                onCheckedChanged = { checked ->
                                    onCategoryCheckedChanged(item.categoryId, checked)
                                },
                            )
                        }
                    }
                    is ExportItem.Shortcut -> {
                        item(
                            key = "shortcut_${item.shortcutId}",
                            contentType = "shortcut",
                        ) {
                            ShortcutListItem(
                                name = item.name,
                                icon = item.icon,
                                checked = item.checked,
                                onCheckedChanged = { checked ->
                                    onShortcutCheckedChanged(item.shortcutId, checked)
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryHeader(
    modifier: Modifier = Modifier,
    name: String,
    checked: Boolean,
    onCheckedChanged: (Boolean) -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {}
            .toggleable(
                value = checked,
                onValueChange = onCheckedChanged,
            )
            .padding(Spacing.MEDIUM),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.MEDIUM),
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = null,
        )

        Text(
            text = name,
            fontSize = FontSize.BIG,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun ShortcutListItem(
    name: String,
    icon: ShortcutIcon?,
    checked: Boolean,
    onCheckedChanged: (Boolean) -> Unit,
) {
    Column {
        ListItem(
            modifier = Modifier
                .fillMaxWidth()
                .semantics(mergeDescendants = true) {}
                .toggleable(
                    value = checked,
                    onValueChange = onCheckedChanged,
                ),
            headlineContent = {
                Text(
                    text = name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            leadingContent = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.MEDIUM),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = checked,
                        onCheckedChange = null,
                    )

                    ShortcutIcon(icon ?: ShortcutIcon.NoIcon)
                }
            },
        )
        HorizontalDivider(color = DividerDefaults.color.copy(alpha = 0.3f))
    }
}

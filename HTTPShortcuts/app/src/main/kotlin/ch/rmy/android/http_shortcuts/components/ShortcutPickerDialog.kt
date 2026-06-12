package ch.rmy.android.http_shortcuts.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.dtos.ShortcutPlaceholder
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon

@Composable
fun ShortcutPickerDialog(
    title: String? = null,
    shortcuts: List<ShortcutPlaceholder>,
    includeCurrentShortcutOption: Boolean = false,
    onShortcutSelected: (ShortcutId) -> Unit,
    onCurrentShortcutSelected: () -> Unit = {},
    onDismissRequested: () -> Unit,
) {
    SelectDialog(
        title = title,
        scrolling = false,
        onDismissRequest = onDismissRequested,
    ) { horizontalPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            if (includeCurrentShortcutOption) {
                item(key = "current") {
                    SelectDialogEntry(
                        horizontalPadding = horizontalPadding,
                        label = stringResource(R.string.label_insert_action_code_for_current_shortcut),
                        onClick = onCurrentShortcutSelected,
                    )
                }
            }
            items(
                items = shortcuts,
                key = { it.id },
            ) { shortcut ->
                SelectDialogEntry(
                    horizontalPadding = horizontalPadding,
                    label = shortcut.name,
                    description = shortcut.description,
                    shortcutIcon = shortcut.icon,
                    onClick = {
                        onShortcutSelected(shortcut.id)
                    },
                )
            }
        }
    }
}

@Preview
@Composable
private fun ShortcutPickerDialog_Preview() {
    ShortcutPickerDialog(
        shortcuts = listOf(
            ShortcutPlaceholder(
                id = "036c34cc-5a18-49d1-afca-fc1195d0ba08",
                name = "Shortcut 1",
                description = "",
                icon = ShortcutIcon.BuiltInIcon("flat_color_rocket"),
            ),
            ShortcutPlaceholder(
                id = "672ca7ec-1f12-4304-9cb2-45bde01c1928",
                name = "Shortcut 2",
                description = "",
                icon = ShortcutIcon.BuiltInIcon("flat_color_rocket"),
            ),
        ),
        onShortcutSelected = {},
        onDismissRequested = {},
    )
}

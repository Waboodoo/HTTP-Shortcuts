package ch.rmy.android.http_shortcuts.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.DialogProperties
import ch.rmy.android.framework.extensions.addOrRemove
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.components.models.MenuEntry
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon

@Composable
fun <T : Any> MultiSelectDialog(
    title: String? = null,
    entries: List<MenuEntry<T>>,
    initiallyChecked: Collection<T> = emptySet(),
    confirmButtonLabel: String = stringResource(R.string.dialog_ok),
    allowEmpty: Boolean = false,
    dismissButton: @Composable (() -> Unit)? = null,
    onDismissRequest: (selected: List<T>?) -> Unit,
) {
    val selectedKeys = remember {
        mutableStateListOf<T>()
            .apply {
                addAll(initiallyChecked)
            }
    }

    AlertDialog(
        modifier = Modifier.padding(Spacing.MEDIUM),
        properties = DialogProperties(usePlatformDefaultWidth = false),
        onDismissRequest = {
            onDismissRequest(null)
        },
        title = title?.let {
            {
                Text(title)
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth(),
            ) {
                items(
                    items = entries,
                    key = { it.key },
                ) { entry ->
                    SelectDialogEntry(
                        label = entry.name,
                        checked = entry.key in selectedKeys,
                        shortcutIcon = entry.icon,
                        onClick = {
                            selectedKeys.addOrRemove(entry.key)
                        },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = allowEmpty || selectedKeys.isNotEmpty(),
                onClick = {
                    onDismissRequest(selectedKeys)
                },
            ) {
                Text(confirmButtonLabel)
            }
        },
        dismissButton = dismissButton,
    )
}

@Preview
@Composable
private fun MultiSelectDialog_Preview() {
    MultiSelectDialog(
        title = "Multi-Select",
        entries = listOf(
            MenuEntry(0, "Entry 1", ShortcutIcon.NoIcon),
            MenuEntry(0, "Entry 2", ShortcutIcon.NoIcon),
        ),
        onDismissRequest = {},
    )
}

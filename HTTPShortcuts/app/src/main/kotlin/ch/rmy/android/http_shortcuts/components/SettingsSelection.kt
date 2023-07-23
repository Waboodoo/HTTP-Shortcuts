package ch.rmy.android.http_shortcuts.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun <T> SettingsSelection(
    title: String,
    icon: ImageVector,
    selectedKey: T,
    items: List<Pair<T, String>>,
    onItemSelected: (T) -> Unit,
) {
    var dialogVisible by remember {
        mutableStateOf(false)
    }

    SettingsButton(
        title = title,
        subtitle = items.find { it.first == selectedKey }?.second,
        icon = icon,
        onClick = {
            dialogVisible = true
        },
    )

    if (!dialogVisible) {
        return
    }

    SelectDialog(
        title = title,
        onDismissRequest = {
            dialogVisible = false
        },
    ) {
        items.forEach { (key, label) ->
            SelectDialogEntry(
                label = label,
                checked = key == selectedKey,
                useRadios = true,
                onClick = {
                    onItemSelected(key)
                    dialogVisible = false
                },
            )
        }
    }
}

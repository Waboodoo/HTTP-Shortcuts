package ch.rmy.android.http_shortcuts.components

import androidx.compose.runtime.Composable
import ch.rmy.android.http_shortcuts.components.models.MenuEntry
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import ch.rmy.android.http_shortcuts.data.dtos.ShortcutPlaceholder

@Composable
fun ShortcutsPickerDialog(
    title: String,
    shortcuts: List<ShortcutPlaceholder>,
    allowEmpty: Boolean = true,
    onConfirmed: (List<ShortcutId>) -> Unit,
    onDismissRequested: () -> Unit,
) {
    MultiSelectDialog(
        title = title,
        allowEmpty = allowEmpty,
        onDismissRequest = {
            if (it != null) {
                onConfirmed(it)
            } else {
                onDismissRequested()
            }
        },
        entries = shortcuts.map { shortcut ->
            MenuEntry(
                key = shortcut.id,
                name = shortcut.name,
                icon = shortcut.icon,
            )
        },
    )
}

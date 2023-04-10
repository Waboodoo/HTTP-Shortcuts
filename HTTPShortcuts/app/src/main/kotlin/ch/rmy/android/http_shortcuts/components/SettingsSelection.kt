package ch.rmy.android.http_shortcuts.components

import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import ch.rmy.android.framework.extensions.indexOfFirstOrNull
import com.alorma.compose.settings.storage.base.rememberIntSettingState
import com.alorma.compose.settings.ui.SettingsList

@Composable
fun <T> SettingsSelection(
    title: String,
    icon: ImageVector,
    selectedKey: T,
    items: List<Pair<T, String>>,
    onItemSelected: (T) -> Unit,
) {
    SettingsList(
        icon = { Icon(icon, contentDescription = title) },
        title = {
            Text(title)
        },
        state = rememberIntSettingState(items.indexOfFirstOrNull { it.first == selectedKey } ?: 0),
        items = items.map { it.second },
        closeDialogDelay = 0,
        onItemSelected = { index, _ ->
            onItemSelected(items[index].first)
        },
    )
}

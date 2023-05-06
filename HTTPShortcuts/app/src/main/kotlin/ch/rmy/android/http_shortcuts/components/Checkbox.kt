package ch.rmy.android.http_shortcuts.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.alorma.compose.settings.storage.base.rememberBooleanSettingState
import com.alorma.compose.settings.ui.SettingsCheckbox

@Composable
fun Checkbox(
    label: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    SettingsCheckbox(
        title = {
            Text(label)
        },
        subtitle = subtitle?.let {
            {
                Text(it)
            }
        },
        state = rememberBooleanSettingState(checked),
        onCheckedChange = onCheckedChange,
    )
}
